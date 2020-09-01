<?php
require_once("url_manager.php");
$get_url = $_GET["url"];
$url = "http://".$vps_url."/nty_img/uploads/".$get_url;
$im = imagecreatefromjpeg($url);
$img_width = imagesx($im);
$img_height = imagesy($im);
$color = imagecolorallocate($im, 128	, 128	, 128);
imagefilledrectangle ($im, 0, $img_height-($img_height/5), $img_width, $img_height, $color);
imagejpeg($im);
