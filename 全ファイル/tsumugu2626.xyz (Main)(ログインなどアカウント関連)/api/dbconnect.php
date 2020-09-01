<?php
//パスワードとかはひみつだよ。

$mysqli = new mysqli($host, $username, $password, $dbname);
if ($mysqli->connect_error) {
  //error_log($mysqli->connect_error);
  exit;
}
//文字化け対策
$mysqli->query("set names utf8mb4");
