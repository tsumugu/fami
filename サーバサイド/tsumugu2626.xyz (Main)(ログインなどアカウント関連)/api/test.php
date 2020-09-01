<?php
/*
$firebase_token = "cU52AAeRbZ4:APA91bGepVZc_sRTfRakrSWK7wRWYNrU8GIyO5fAKUb0QssZvYUQlPClSCGMucpGj-tW-GQxlwrcIrTjZSO-PIcLnOVjq-EQ0r-Jz26HR7N65UhviJOWD1UHoKieTvFO2dP3B0kZvT7l";
$message = "test";
$api_key = "AAAAeqetIJU:APA91bFID_1mAmYt1LjxIjhHv_YLy_KunfKfjZwO6VdTgyHyNXY7NmEHAz7ftfMY95vUlfZ0mFP8Ohg-0ifRplrT-p7avjFeCmjhLigCXTAWkkJoq4N6YDqe_Ob9RBcr4jSv6TYkg6S0z2Ej-Q_We6c8cCJCe1NfXg";
$url = "https://fcm.googleapis.com/fcm/send";
$headers = [
  "Authorization: key=".$api_key,
  "Content-Type: application/json"
];
$fields = [
  "to" => $firebase_token,
  "priority" => "high",
  "notification" => [
    "text" => $message
  ]
];
$handle = curl_init();
curl_setopt($handle, CURLOPT_URL, $url);
curl_setopt($handle, CURLOPT_POST, true);
curl_setopt($handle, CURLOPT_HTTPHEADER, $headers);
curl_setopt($handle, CURLOPT_RETURNTRANSFER, true);
curl_setopt($handle, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($handle, CURLOPT_POSTFIELDS, json_encode($fields));
$result = curl_exec($handle);
curl_close($handle);
echo $result;
*/
$to = "tsumugu1515@gmail.com";
$from = "send-only@tsumugu2626.xyz";
$from_name = "Fami";
$subject = "メールアドレスの確認";
$body = "Famiへの登録ありがとうございます。\n24時間以内に以下のURLにアクセスしてメールアドレスの確認を完了させてください。\nhttp://tsumugu2626.xyz/nty/check?t=".md5(time()+rand());

mb_language("ja");
mb_internal_encoding('utf-8');
$head  = "From: " . mb_encode_mimeheader(mb_convert_encoding($from_name,"ISO-2022-JP")) . "<{$from}> \n";
if (!mb_send_mail($to, $subject, $body, $head)) {
  echo "送信エラー";
}