<?php
/*
include_once 'dbconnect.php';
//
$email = $_GET["email"];
$is_post_error = false;
if (!isset($email) || $email === '') {
  $is_post_error = true;
}
if (!preg_match('|^[0-9a-z_./?-]+@([0-9a-z-]+\.)+[0-9a-z-]+$|', $email)) {
  $is_post_error = true;
}
if (!$is_post_error) {
  //1. emailでmail_tokenを検索
  $query = "SELECT * FROM mail_token WHERE email='".$email."'";
  if($res = $mysqli->query($query)) {
    foreach ($res as $r) {
      $uid = $r["userid"];
      if (!empty($uid)) {
        break;
      }
    }
  }
  if (!empty($uid)) {
    $nowtime = time();
    $mailtkn = md5($nowtime.rand());
    $query = "INSERT INTO mail_token(token,userid,email,created_date) VALUES('$mailtkn','$uid','$email','$nowtime')";
      if($mysqli->query($query)) {
        $from = "send-only@tsumugu2626.xyz";
        $from_name = "Fami";
        $subject = "[再送]メールアドレスの確認";
        $body = "Famiへの登録ありがとうございます。\n24時間以内に以下のURLにアクセスしてメールアドレスの確認を完了させてください。\nhttp://tsumugu2626.xyz/nty_login/check/?t=".$mailtkn;
        $head  = "From: " . mb_encode_mimeheader(mb_convert_encoding($from_name,"ISO-2022-JP")) . "<{$from}> \n";
        if (mb_send_mail($email, $subject, $body, $head)) {
          $message = "登録が完了しました！";
          $status_id = 1;
        } else {
          $message = "サーバでエラーが発生しました";
          $status_id = 3;
        }
      }
  } else {
    //メアドnotfound
    $message = "メールアドレスが見つかりませんでした";
    $status_id = 2;
  }
} else {
  $message = "メールアドレスが見つかりませんでした";
  $status_id = 2;
}
$js = array(
  "id" => $status_id,
  "message" => $message
);
echo json_encode($js);
*/
