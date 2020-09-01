<?php
// DBとの接続
include_once 'dbconnect.php';

$can_regist = true;
$message = "";
$status_id = 0;

//渡されたデータを確認
$is_post_error = false;
if (!isset($_GET['email']) || $_GET['email'] === '' || !isset($_GET['password']) || $_GET['password'] === '') {
  $is_post_error = true;
}
if (!preg_match('|^[0-9a-z_./?-]+@([0-9a-z-]+\.)+[0-9a-z-]+$|', $_GET['email'])) {
  $is_post_error = true;
  $can_regist = false;
}
if (!preg_match("/^[a-zA-Z0-9\/*-+.,!#$%&()~|_]+$/", $_GET['password'])) {
  $is_post_error = true;
  $can_regist = false;
}
if ($is_post_error) {
  $message = "メールアドレスとパスワードを正しく入力してください";
  $status_id = 2;
}
//
$email = $mysqli->real_escape_string($_GET['email']);
//メアド重複チェック
$query = "SELECT * FROM users WHERE email = '".$email."'";
$unandmct = 0;
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  if ($mail["email"] == $_GET['email']) {
    $unandmct++;
  }
}
if ($unandmct !== 0) {
  //エラーメッセージ
  $message = "このメールアドレスはすでに登録されています";
  $status_id = 3;
  $can_regist = false;
}
if ($can_regist) {
  //$password = $mysqli->real_escape_string($_POST['password']);
  $password = $_GET['password'];
  $password = password_hash($password, PASSWORD_DEFAULT);
  $nowtime = time();
  $uid = md5($nowtime.rand());
  // POSTされた情報をDBに格納する
  $query = "INSERT INTO users(email,password,userid,is_formal_registration,is_allow_send_mail) VALUES('$email','$password','$uid','false','')";
  if($mysqli->query($query)) {
    //TODO: 確認メールを送信
    $mailtkn = md5(time().rand());
    $query = "INSERT INTO mail_token(token,userid,email,created_date) VALUES('$mailtkn','$uid','$email','$nowtime')";
    if($mysqli->query($query)) {
      $message = "登録が完了しました！".$_GET['email'];
      $status_id = 1;
      //メール送信
      if (strpos($_GET['email'],'@tsumugu2626.xyz') === false) {
        $from = "send-only@tsumugu2626.xyz";
        $from_name = "Fami";
        $subject = "メールアドレスの確認";
        $body = "Famiへの登録ありがとうございます。\n24時間以内に以下のURLにアクセスしてメールアドレスの確認を完了させてください。\nhttp://tsumugu2626.xyz/nty_login/check/?t=".$mailtkn;
        $head  = "From: " . mb_encode_mimeheader(mb_convert_encoding($from_name,"ISO-2022-JP")) . "<{$from}> \n";
        mb_send_mail($_GET['email'], $subject, $body, $head);
      }
    } else {
      $message = "サーバでエラーが発生しました";
      $status_id = 4;
    }
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
