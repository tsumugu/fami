<?php
//セキュリティーガバガバなので何とかする。
$face_img_name = basename($_GET["file_name"]);
//ディレクトリトラバーサル対策
$now_path = "tmp/".$face_img_name.".jpeg";
$exp = explode("-",$face_img_name);
$move_to_path = "uploads/".$exp[0]."-".$exp[1].".jpeg";
if (file_exists($now_path)) {
  //mv
  if (@rename($now_path, $move_to_path)) {
    //@file_put_contents("score/".$exp[0]."-".$exp[1].".txt",$face_score);
    $ret_json_arr = array(
      "id" => 1,
      "message" => "Succeed".$face_score
    );
  } else {
    $ret_json_arr = array(
      "id" => 2,
      "message" => "ErrorOccurred"
    );
  }
  //score計算
  @file_put_contents("score_wait_list.txt",$move_to_path."\n",FILE_APPEND);
  //exec("nohup php get_diff_php.php {$move_to_path}");
  /*
  exec("/usr/bin/docker run --rm -v /var/www/html:/var/www/html lapidarioz/docker-python-opencv3-dlib python /var/www/html/nty_img/get_diff.py /var/www/html/nty_img/{$move_to_path}  2>&1", $output, $return_var);
  $ret_json = @json_decode($output[0],true);
  $face_detect_status = $ret_json["status"];
  $face_score = $ret_json["score"];
  if ($return_var !== 0 || $face_detect_status !==  "OK") {
    $ret_json_arr = array(
      "id" => 2,
      "message" => "PythonErrorOccurred".$output[0]
    );    
  } else {
    @file_put_contents("score/".$exp[0]."-".$exp[1].".txt",$face_score);
    $ret_json_arr = array(
      "id" => 1,
      "message" => "Succeed".$face_score
    );
  }
  */
} else {
  $ret_json_arr = array(
    "id" => 2,
    "message" => "FileNotFound"
  );
}
echo json_encode($ret_json_arr);
