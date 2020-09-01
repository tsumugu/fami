<?php
include_once '../api/dbconnect.php';
$get_token = $_GET["t"];
$html = '<meta name="viewport" content="width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no">';
$query = "SELECT * FROM mail_token WHERE token = '".$get_token."'";
$result = $mysqli->query($query);
if ($result) {
  while ($row = $result->fetch_assoc()) {
    $db_created_date = (int)$row['created_date'];
    $db_uid = $row['userid'];
    $diff = time() - $db_created_date;
    //24時間以内だったら
    if ($diff < 60 * 60 * 24) {
      //消す
      $query = "DELETE FROM mail_token WHERE token = '".$get_token."'";
      if($mysqli->query($query)) {
        //書き換える
        $query = "UPDATE users SET is_formal_registration = 'true' WHERE userid = '".$db_uid."'";
        if($mysqli->query($query)) {
          //飛ばす
          echo $html.'メールアドレスの確認が完了しました！<br><a href="fami-app://login?f=mail">ログイン画面を開く</a>';
          //header("Location: fami-app://login?f=mail");
          exit;
        }
      } else {
        echo $html."サーバで問題が発生しました。";
        exit;
      }
    } else {
      echo $html."有効期限を過ぎています。";
      exit;
    }
  }
  echo $html."URLに問題があります。";
} else {
  echo $html."URLに問題があります。";
  exit;
}
