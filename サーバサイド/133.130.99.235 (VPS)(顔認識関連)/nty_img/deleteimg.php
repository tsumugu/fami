<?php
$family_id = $_GET["family_id"];
$uid = $_GET["user_id"];
$file_path = "/var/www/html/nty_img/uploads/".$family_id."-".$uid.".jpeg";
if (file_exists($file_path)) {
  unlink($file_path);
}
$file_path = "/var/www/html/nty_img/score/".$family_id."-".$uid.".txt";
if (file_exists($file_path)) {
  unlink($file_path);
}
