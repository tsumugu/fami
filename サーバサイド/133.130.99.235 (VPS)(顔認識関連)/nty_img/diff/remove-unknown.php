<?php
date_default_timezone_set('Asia/Tokyo');
$filename = basename($_GET["fn"]);
$path = "/var/www/html/nty_img/diff/unknown_text/".$filename;
$new_path = "/var/www/html/nty_img/diff/checked_text/".$filename;
if (file_exists($path)) {
  if (@rename($path, $new_path)) {
    echo 'ok';
  } else {
    echo 'ng';
  }
} else {
  echo 'ng';
}
