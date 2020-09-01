<?php
//DBに入外出履歴を保存
include_once 'dbconnect.php';
$token = $_GET["token"];
$familyid = $_GET["family_id"];
$can_regist = true;
$message = "";
$status_id = 0;
//存在確認
if (!isset($token) || $token === '' || !isset($familyid) || $familyid === '') {
  $can_regist = false;
  $message = "正しく入力してください";
  $status_id = 2;
}
//userid確認
$query = "SELECT * FROM family";
$unandmct = 0;
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  //family_idとtoken(userid)が逆
  if ($mail["family_id"] == $token) {
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
  $token = $mysqli->real_escape_string($token);
  $familyid = $mysqli->real_escape_string($familyid);
  $ts = time();
  //前回と逆
  $sql = "SELECT * FROM iohistory";
  $query = $mysqli->query($sql);
  $id = "";
  $arr = array();
  $arr2 = array();
  if($query) {
    foreach ($query as $q) {
      if ($q["userid"] == $token && $q["family_id"] == $familyid) {
        //
        $arr[] = $q["status"];
        $arr2[] = $q["timestamp"];
      }
    }
  }
  $arr = array_reverse($arr);
  $arr2 = array_reverse($arr2);
  $ts_diff = (int)$ts - (int)$arr2[0];
  if ($ts_diff < 0) {
    $ts_diff = 31;
  }
  if (10 < $ts_diff) {
    $id = $arr[0];
    if ($id === "1") {
      $id = "0";
    } else if ($id === "0") {
      $id = "1";
    } else {
      $id = "1";
    }
    $io_status = $id;
        //$query = "DELETE FROM iohistory WHERE userid=? AND family_id=?";
        //$stmt = $mysqli->prepare($query);
        //$stmt->bind_param('ss',$token,$familyid);
        //$query_res = $stmt->execute();
        //
        //$stmt->close();
        $query = "INSERT INTO iohistory(userid,family_id,timestamp,status) VALUES('$token','$familyid','$ts','$id')";
        $query_res = $mysqli->query($query);
        //
      if ($query_res) {
        //メールを送る
        $query_s = "SELECT * FROM users WHERE userid = '".$familyid."'";
        if($query_did = $mysqli->query($query_s)) {
          if (count($query_did) !== 0) {
            foreach ($query_did as $q_s) {
              $is_allow_send_email = $q_s["is_allow_send_mail"];
              if ($is_allow_send_email !== "false") {
                $mail_address = $q_s["email"];
                $family_name = @json_decode(@file_get_contents("http://tsumugu2626.xyz/nty_login/api/get_family_name.php?familyid={$familyid}&userid={$token}"), true);
                $family_name = $family_name["familyname"];
                if ($id == "0") {
                  $st_str = "帰宅";
                } else if ($id == "1") {
                  $st_str = "外出";
                } else {
                  $st_str = null;
                }
                if (!empty($family_name) && !empty($st_str)) {
                  $from = "send-only@tsumugu2626.xyz";
                  $from_name = "Fami";
                  $subject = "{$family_name}さんが{$st_str}しました";
                  $body = date("Y/m/d H:i", $ts)."に{$family_name}さんが{$st_str}しました";
                  $stop_url = "http://secure.tsumugu2626.xyz/nty_login/stop/?t={$familyid}";
                  $body .= "\n----------------\nこのメールの配信を停止する:\n{$stop_url}\n\n※このメールアドレスは送信専用です\n----------------\n";
                  $head  = "From: " . mb_encode_mimeheader(mb_convert_encoding($from_name,"ISO-2022-JP")) . "<{$from}> \n";
                  if (mb_send_mail($mail_address, $subject, $body, $head)) {
                    $message = "成功しました！/{$id}";
                    $status_id = 1;
                  } else {
                    $message = "サーバでエラーが発生しました";
                    $status_id = 4;
                  }
                } else {
                  $message = "サーバでエラーが発生しました";
                  $status_id = 4;
                }
              } else {
                $message = "メール送信が拒否されました";
                $status_id = 4;
              }
            }
          } else {
            $message = "サーバでエラーが発生しました";
            $status_id = 4;
          }
        } else {
          $message = "サーバでエラーが発生しました";
          $status_id = 4;
        }
        /*
        //通知を送る
        $query_s = "SELECT * FROM firebase_token WHERE userid = '".$token."' AND family_id = '".$familyid."'";
        if($query_did = $mysqli->query($query_s)) {
          $api_key = "AAAAeqetIJU:APA91bFID_1mAmYt1LjxIjhHv_YLy_KunfKfjZwO6VdTgyHyNXY7NmEHAz7ftfMY95vUlfZ0mFP8Ohg-0ifRplrT-p7avjFeCmjhLigCXTAWkkJoq4N6YDqe_Ob9RBcr4jSv6TYkg6S0z2Ej-Q_We6c8cCJCe1NfXg";
          $url = "https://fcm.googleapis.com/fcm/send%22;
          $headers = [
            "Authorization: key=".$api_key,
            "Content-Type: application/json"
          ];
          if (count($query_did) !== 0) {
            foreach ($query_did as $q_s) {
              //family_idから名前取得
              $family_name = "";
              $query_f = "SELECT * FROM family WHERE family_id = '".$familyid."'";
              if($query_f_did = $mysqli->query($query_f)) {
                if (count($query_f_did) !== 0) {
                  foreach ($query_f_did as $q_fd) {
                    $family_name = $q_fd["name"];
                  }
                }
              }
              if ($id === "1") {
                $s_str = "外出";
              } else {
                $s_str = "帰宅";
              }
              //
              $message = "{$family_name}さんが{$s_str}しました";
              $firebase_token = $q_s["token"];
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
            }
          }
          $message = "成功しました！";
          $status_id = 1;
        } else {
          $message = "サーバでエラーが発生しました";
          $status_id = 4;
        }
        //
        */
      } else {
        $message = "サーバでエラーが発生しました";
        $status_id = 4;
      }
  } else {
    $message = "30s";
    $status_id = 4;
  }
}
$js = array(
  "id" => $status_id,
  "message" => $message,
  "io_status"=>$io_status
);
echo json_encode($js);
