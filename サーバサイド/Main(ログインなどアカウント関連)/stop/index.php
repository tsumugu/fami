<?php
include_once '../api/dbconnect.php';
$get_token = $_GET["t"];
$flag = $_GET["f"];
$ts = $_GET["ts"];
$r = $_GET["r"];
$html = '<meta name="viewport" content="width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no">';
$query = "SELECT * FROM users WHERE userid = '".$get_token."'";
$result = $mysqli->query($query);
if ($result) {
  while ($row = $result->fetch_assoc()) {
    if ($flag === "t") {
      if ((time()-(int)$ts)<60) {
        //処理する
        if ($row["is_allow_send_mail"] !== "false" || $r === "t") {
          $db_uid = $row["userid"];
          if($mysqli->query($query)) {
            //書き換える
            if ($r === "t") {
              $query = "UPDATE users SET is_allow_send_mail = 'true' WHERE userid = '".$db_uid."'";
              $mes = '配信を有効にしました';
            } else {
              $query = "UPDATE users SET is_allow_send_mail = 'false' WHERE userid = '".$db_uid."'";
              $mes = '配信を停止しました';
            }
            if($mysqli->query($query)) {
              //飛ばす
              echo $html.$mes;
              exit;
            } else {
              echo $html."サーバで問題が発生しました。";
              exit;
            }
          } else {
            echo $html."サーバで問題が発生しました。";
            exit;
          }
        }
      } else {
        //時間超過
        echo $html."タイムアウトしました。もう一度やり直してください";
        exit;
      }
    } else {
      //リンク表示
      $timestamp = time();
      if ($row["is_allow_send_mail"] === "false") {
        echo $html."配信はすでに停止されています。再び有効にしますか？<br><a href=\"http://secure.tsumugu2626.xyz/nty_login/stop/?t={$get_token}&f=t&r=t&ts={$timestamp}\">有効にする</a>";
      } else {
        echo $html."本当にメールの配信を停止しますか？<br><a href=\"http://secure.tsumugu2626.xyz/nty_login/stop/?t={$get_token}&f=t&ts={$timestamp}\">停止する</a>";
      }
      exit;
    }
  }
} else {
  echo $html."URLに問題があります。";
  exit;
}
