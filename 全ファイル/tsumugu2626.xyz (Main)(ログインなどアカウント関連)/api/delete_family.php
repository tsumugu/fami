<?php
include_once 'dbconnect.php';
//token=userid
$family_id = $_GET["token"];
$user_id = $_GET["uid"];
$family_id = str_replace(array(" ","　"),"",$family_id);
$user_id = str_replace(array(" ","　"),"",$user_id);
$can_func = true;
$message = "";
$status_id = 0;
//存在確認
if (!isset($family_id) || $family_id === '' || !isset($user_id) || $user_id === '') {
  $can_func = false;
  $message = "内部エラーが発生しました";
  $status_id = 2;
}
//userid確認
$query = "SELECT * FROM users";
$unandmct = 0;
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  if ($mail["userid"] == $family_id) {
    $unandmct++;
  }
}
if ($unandmct == 0) {
  //エラーメッセージ
  $message = "tokenエラー";
  $status_id = 3;
  $can_func = false;
}
//DBに登録
if ($can_func) {
  $family_id = $mysqli->real_escape_string($family_id);
  $user_id = $mysqli->real_escape_string($user_id);
  $query = "DELETE FROM family WHERE token = '".$family_id."' AND family_id = '".$user_id."'";
  if($mysqli->query($query)) {
    //アイコン削除処理
    @file_get_contents("http://133.130.99.235/nty_img/deleteimg.php?family_id={$family_id}&user_id={$user_id}");
    //
    $message = "削除しました";
    $status_id = 1;
  } else {
    $message = "サーバでエラーが発生しました";
    $status_id = 4;
  }
}
$js = array(
  "id" => $status_id,
  "message" => $message
);
echo json_encode($js);
