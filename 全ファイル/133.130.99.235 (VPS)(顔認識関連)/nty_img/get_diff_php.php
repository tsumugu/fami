<?php
//@file_put_contents("arrive.txt","");
function get_score($path) {
  if (file_exists("/var/www/html/nty_img/".$path)) {
    $exp = explode("/",str_replace(".jpeg","",$path));  
    $exp = explode("-",$exp[1]);
    exec("/usr/bin/docker run --rm -v /var/www/html:/var/www/html lapidarioz/docker-python-opencv3-dlib python /var/www/html/nty_img/get_diff.py /var/www/html/nty_img/{$path}  2>&1", $output, $return_var);
    $ret_json = @json_decode($output[0],true);
    $face_detect_status = $ret_json["status"];
    $face_score = $ret_json["score"];
    if ($return_var !== 0 || $face_detect_status !==  "OK") {
      return false;
    } else {
      //@file_put_contents("score/".$exp[0]."-".$exp[1].".txt",$face_score);
      return $face_score;
    }
  } else {
    return false;
  }
}
$file_lines = file("/var/www/html/nty_img/score_wait_list.txt");
$file_lines = array_unique($file_lines);
$file_lines = array_values($file_lines);
foreach ($file_lines as $k=>$l) {
  $l = str_replace("\n","",$l);
  $exp = explode("/",str_replace(".jpeg","",$l));
  $exp = explode("-",$exp[1]);  
  unset($file_lines[$k]);
  $score = get_score($l);
  var_dump($score);
  if ($score) {
    @file_put_contents("/var/www/html/nty_img/score/".$exp[0]."-".$exp[1].".txt",$score);
  }
}
@file_put_contents("/var/www/html/nty_img/score_wait_list.txt",implode("\n",$file_lines));
