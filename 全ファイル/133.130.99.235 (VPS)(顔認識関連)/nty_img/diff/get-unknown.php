<?php
date_default_timezone_set('Asia/Tokyo');
$family_id = $_GET["familyid"];
$ret_array = array();
foreach (glob("/var/www/html/nty_img/diff/unknown_text/".basename($family_id)."-*.txt") as $f) {
  $read_text = @file_get_contents($f);
  $exp = explode(",",$read_text);
  $timestamp = str_replace(".0","",$exp[0]);
  $date = date("Y/m/d,H:i",$timestamp);
  $img_path = str_replace("/var/www/html/nty_img/","",$exp[1]);
  $r_arr = array(
    "date"=>$date,
    "timestamp"=>(int)$timestamp,
    "img_url"=>$img_path,
    "text_file_name"=>str_replace("/var/www/html/nty_img/diff/unknown_text/","",$f)
  );
  $ret_array[] = $r_arr;
}
echo json_encode(array("count"=>count($ret_array),"info"=>$ret_array));
