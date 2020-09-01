<?php
include_once 'dbconnect.php';
//token=userid
$token = $_GET["token"];
$name = $_GET["name"];
$id = $_GET["id"];
$token = str_replace(array(" ","　"),"",$token);
$name = str_replace(array(" ","　"),"",$name);
$id = str_replace(array(" ","　"),"",$id);
$can_regist = true;
$message = "";
$status_id = 0;
//存在確認
if (!isset($token) || $token === '' || !isset($name) || $name === '' || !isset($id) || $id === '' ) {
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
//(父母)
/*
if(preg_match('/\(父\)|\(母\)|\(子\)|\(祖父\)|\(祖母\)|\(その他\)/',$name)){
  $can_regist = false;
  $message = "使用できない文字列が含まれています";
  $status_id = 2;
}
*/
//userid確認
$query = "SELECT * FROM users";
$unandmct = 0;
$is_first = "false";
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  if ($mail["userid"] == $token) {
    $unandmct++;
  }
}
if ($unandmct == 0) {
  //エラーメッセージ
  $message = "tokenエラー";
  $status_id = 3;
  $can_regist = false;
}
//人数が上限に達していないかチェック
$query = "SELECT * FROM family";
$cct = 0;
$c_qs = $mysqli->query($query);
foreach ($c_qs as $cq) {
  if ($cq["token"] === $token) {
    $cct++;
  }
}
if ($cct>7) {
  $can_regist = false;
  $message = "登録が可能な人数は8人までです";
  $status_id = 2;
}
//DBに登録
if ($can_regist) {
  $token = $mysqli->real_escape_string($token);
  $name = $mysqli->real_escape_string($name);
  $id = $mysqli->real_escape_string($id);
  $fid = md5(time()+rand());
  $query = "INSERT INTO family(token,name,id,family_id) VALUES('$token','$name','$id','$fid')";
  //
  if($mysqli->query($query)) {
    //カウント、1つならメッセージを変える
    $select_query = "SELECT * FROM `family` WHERE `token`='".$token."'";
    $count=0;
    foreach ($mysqli->query($select_query) as $s) {
      $count++;
    }
    if ($count == 1) {
      //$message = "追加に成功しました！戻るボタンからログインユーザを選択してください";
      $message = "追加に成功しました！";
      $is_first = "true";
    } else {
      $message = "追加に成功しました！";
    }
    $status_id = 1;
  } else {
    $message = "サーバでエラーが発生しました";
    $status_id = 4;
  }
}
$js = array(
  "id" => $status_id,
  "message" => $message,
  "is_first" => $is_first
);
echo json_encode($js);
