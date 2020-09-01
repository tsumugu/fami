<?php
//今はメンテナンス中じゃない
$mnt = "false";
//メンテナンスのタイトル
$mnt_title = "緊急メンテナンス実施中";
//メンテナンスの説明
$mnt_mes = "現在緊急メンテナンスを実施中です。ご協力お願いします";
//
$array = array(
  "is_mnt" => $mnt,
  "mnt_title" => $mnt_title,
  "mnt_mes" => $mnt_mes
);
echo json_encode($array);
