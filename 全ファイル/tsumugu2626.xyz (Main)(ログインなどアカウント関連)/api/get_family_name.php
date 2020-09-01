<?php
include_once 'dbconnect.php';
$familyid = $_GET["familyid"];
$familyid = str_replace(array(" ","　"),"",$familyid);
$userid = $_GET["userid"];
$userid = str_replace(array(" ","　"),"",$userid);
$can_func = true;
$message = "";
$status_id = 0;
$list = array();
//存在確認
if (!isset($familyid) || $familyid === '' || !isset($userid) || $userid === '') {
  $can_func = false;
  $message = "pramエラー";
  $status_id = 2;
}
//userid確認
$query = "SELECT * FROM users";
$unandmct = 0;
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  if ($mail["userid"] == $familyid) {
    $unandmct++;
  }
}
if ($unandmct == 0) {
  //エラーメッセージ
  $message = "pramエラー";
  $status_id = 2;
  $can_func = false;
}
//DB
if ($can_func) {
  $sql = "SELECT * FROM family";
  $query = $mysqli->query($sql);
  if($query) {
    foreach ($query as $q) {
      //family_idとtoken逆
      if ($q["family_id"] == $userid && $q["token"] == $familyid) {
        $fami_name = $q["name"];
        $message="取得成功";
        $status_id = 1;
        break;
      }
    }
  } else {
    $message = "サーバエラー";
    $status_id = 3;
  }
}
$js = array(
  "id" => $status_id,
  "message" => $message,
  "familyname" => $fami_name
);
echo json_encode($js);
