<?php
include_once 'dbconnect.php';
include_once 'url_manager.php';
//token=userid
$token = $_GET["token"];
$token = str_replace(array(" ","　"),"",$token);
$can_func = true;
$is_ok = true;
$message = "";
$status_id = 0;
$list = array();
//
function getStatusCode($url) {
  $header = null;
  $options = array(
   CURLOPT_RETURNTRANSFER => true,
   CURLOPT_HEADER         => true,
   CURLOPT_FOLLOWLOCATION => true,
   CURLOPT_ENCODING       => "",
   CURLOPT_USERAGENT      => "spider",
   CURLOPT_SSL_VERIFYPEER => false,
   CURLOPT_SSL_VERIFYHOST => false,
   CURLOPT_AUTOREFERER    => true,
   CURLOPT_CONNECTTIMEOUT => 120,
   CURLOPT_TIMEOUT        => 120,
   CURLOPT_MAXREDIRS      => 10,
  );
  $ch = curl_init($url);
  curl_setopt_array($ch, $options);
  $content = curl_exec($ch);

  if(!curl_errno($ch)) {
   $header = curl_getinfo($ch);
  }// end if
  curl_close($ch);
  return $header['http_code'];
}
//
//存在確認
if (!isset($token) || $token === '') {
  $can_func = false;
  $message = "tokenエラー";
  $status_id = 2;
}
//userid確認
$query = "SELECT * FROM users";
$unandmct = 0;
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  if ($mail["userid"] == $token) {
    $unandmct++;
  }
}
if ($unandmct == 0) {
  //エラーメッセージ
  $message = "tokenエラー";
  $status_id = 2;
  $can_func = false;
}
if ($can_func) {
  //DBからfamily_idを取得、user_id-family_id.jpegにアクセスして200が帰ってきているかを確認、帰ってきてなかったらfalseに
  $token = $mysqli->real_escape_string($token);
  $sql = "SELECT * FROM family WHERE token = '".$token."'";
  $query = $mysqli->query($sql);
  if($query) {
    foreach ($query as $q) {
      $get_img_file_url = "http://{$vps_url}/nty_img/uploads/".$token."-".$q["family_id"].".jpeg";
      $st_code = getStatusCode($get_img_file_url);
      if ($st_code !== 200) {
        $list[] = $q["family_id"];
        $is_ok = false;
      }
    }
    if($is_ok) {
      $message = "全ての画像がアップロードされています";
      $status_id = 1;
    } else {
      $message = "一部画像がアップロードされていません";
      $status_id = 2;
    }
  } else {
    $message = "サーバでエラーが発生しました";
    $status_id = 3;
  }
}
$js = array(
  "id" => $status_id,
  "message" => $message,
  "family_list" => $list
);
echo json_encode($js);
