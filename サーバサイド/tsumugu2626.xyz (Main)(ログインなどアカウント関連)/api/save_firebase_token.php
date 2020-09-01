<?php
include_once 'dbconnect.php';
//token=userid
$token = $_GET["token"];
$userid = $_GET["userid"];
$familyid = $_GET["familyid"];
$token = str_replace(array(" ","　"),"",$token);
$userid = str_replace(array(" ","　"),"",$userid);
$familyid = str_replace(array(" ","　"),"",$familyid);
$can_regist = true;
$message = "";
$status_id = 0;
//存在確認
if (!isset($token) || $token === '' || !isset($userid) || $userid === '' || !isset($familyid) || $familyid === '' ) {
  $can_regist = false;
  $message = "tokenエラー";
  $status_id = 2;
}
//userid確認
$query = "SELECT * FROM users";
$unandmct = 0;
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  if ($mail["userid"] == $userid) {
    $unandmct++;
  }
}
if ($unandmct == 0) {
  //エラーメッセージ
  $message = "tokenエラー";
  $status_id = 2;
  $can_regist = false;
}
if ($can_regist) {
  $token = $mysqli->real_escape_string($token);
  $userid = $mysqli->real_escape_string($userid);
  $familyid = $mysqli->real_escape_string($familyid);
  //一旦削除
  $query = "DELETE FROM firebase_token WHERE userid = '".$userid."' AND family_id = '".$familyid."'";
  if($mysqli->query($query)) {
    //DBに登録
    $query = "INSERT INTO firebase_token(userid,family_id,token) VALUES('$userid','$familyid','$token')";
    if($mysqli->query($query)) {
      $message = "成功";
      $status_id = 1;
    } else {
      $message = "サーバでエラーが発生しました";
      $status_id = 3;
    }
  } else {
    $message = "サーバでエラーが発生しました";
    $status_id = 3;
  }
}
$js = array(
  "id" => $status_id,
  "message" => $message
);
echo json_encode($js);
