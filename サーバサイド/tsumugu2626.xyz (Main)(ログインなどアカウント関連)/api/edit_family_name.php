<?php
include_once 'dbconnect.php';
//token=userid
$family_id = $_GET["family_id"];
$user_id = $_GET["user_id"];
$name = $_GET["name"];
$name = str_replace(array(" ","　"),"",$name);

$can_regist = true;
$message = "";
$status_id = 0;
//存在確認
if (!isset($family_id) || $family_id === '' || !isset($user_id) ||$user_id === '' || !isset($name) || $name === '') {
  $can_regist = false;
  $message = "正しく入力してください";
  $status_id = 2;
}
//名前の文字数チェック
if (mb_strlen($name)>10) {
  $can_regist = false;
  $message = "名前は10文字以内にしてください";
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
  $can_regist = false;
}
//DBに登録
if ($can_regist) {
  $family_id = $mysqli->real_escape_string($family_id);
  $user_id = $mysqli->real_escape_string($user_id);
  $name = $mysqli->real_escape_string($name);
  $fid = md5(time()+rand());
  $query = "UPDATE family SET name='{$name}' WHERE `token`='{$family_id}' AND `family_id`='{$user_id}'";
  //
  if($mysqli->query($query)) {
    $message = "更新に成功しました！";
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
