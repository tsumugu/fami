<?php
//DBに入外出履歴を保存
include_once 'dbconnect.php';
$familyid = $_GET["family_id"];
$can_regist = true;
$message = "";
$status_id = 0;
//存在確認
if (!isset($familyid) || $familyid === '') {
  $can_regist = false;
  $message = "正しく入力してください";
  $status_id = 2;
}
//userid確認
$query = "SELECT * FROM users";
$unandmct = 0;
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  //family_idとtoken(userid)が逆
  if ($mail["userid"] == $familyid) {
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
  $familyid = $mysqli->real_escape_string($familyid);
  $ts = time();
  //メールを送る
  $query_s = "SELECT * FROM users WHERE userid = '".$familyid."'";
  if($query_did = $mysqli->query($query_s)) {
    if (count($query_did) !== 0) {
      foreach ($query_did as $q_s) {
        $mail_address = $q_s["email"];
        $from = "send-only@tsumugu2626.xyz";
        $from_name = "Fami";
        $subject = "【緊急】不審者を検知しました";
        $body = date("Y/m/d H:i", $ts)."に未登録の人物を検知しました。アプリから確認してください";
        $body .= "\n----------------\n※このメールアドレスは送信専用です\n----------------\n";
        $head  = "From: " . mb_encode_mimeheader(mb_convert_encoding($from_name,"ISO-2022-JP")) . "<{$from}> \n";
        if (mb_send_mail($mail_address, $subject, $body, $head)) {
          $message = "成功しました！";
          $status_id = 1;
        } else {
          $message = "サーバでエラーが発生しました";
          $status_id = 4;
        }
      }
    }
  }
}
$js = array(
  "id" => $status_id,
  "message" => $message
);
echo json_encode($js);
