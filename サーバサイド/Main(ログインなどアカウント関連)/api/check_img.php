<?php
include_once 'dbconnect.php';
include_once 'url_manager.php';
$user_id = $_GET["user_id"];
$user_id = str_replace(array(" ","　"),"",$user_id);
$family_id = $_GET["family_id"];
$family_id = str_replace(array(" ","　"),"",$family_id);
$can_func = true;
$message = "";
$status_id = 0;
//存在確認
if (!isset($family_id) || $family_id === '' || !isset($user_id) || $user_id === '') {
  $can_func = false;
  $message = "idエラー";
  $status_id = 3;
}
//user_id確認
$query_u = "SELECT * FROM users";
$unandmct_u = 0;
$mails_u = $mysqli->query($query_u);
foreach ($mails_u as $mail_u) {
  if ($mail_u["userid"] == $user_id) {
    $unandmct_u++;
  }
}
if ($unandmct_u == 0) {
  //エラーメッセージ
  $message = "idエラー";
  $status_id = 3;
  $can_func = false;
}
//familyid確認
$query = "SELECT * FROM family";
$unandmct = 0;
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  if ($mail["family_id"] == $family_id) {
    $unandmct++;
  }
}
if ($unandmct == 0) {
  //エラーメッセージ
  $message = "idエラー";
  $status_id = 3;
  $can_func = false;
}
//存在確認
if ($can_func) {
  $ch = curl_init("http://{$vps_url}/nty_img/uploads/".$user_id."-".$family_id.'.jpeg');
  curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, true );
  curl_exec($ch);
  if (!curl_errno($ch)) {
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    if ($http_code === 200) {
      $message = "OK";
      $status_id = 1;
    } else if ($http_code === 404) {
      $message = "NG";
      $status_id = 2;
    } else {
      $message = "サーバでエラーが発生しました";
      $status_id = 4;
    }
  } else {
    $message = "サーバでエラーが発生しました";
    $status_id = 4;
  }
  curl_close($ch);
}
$js = array(
  "id" => $status_id,
  "message" => $message
);
echo json_encode($js);
