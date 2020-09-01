<?php
require_once 'sort_func.php';
$family_id = $_GET["family_id"];

$dir_path = "../up_audio/";
$file_name_pattern = "{$family_id}-*-*-*.wav";
$pattern = $dir_path.$file_name_pattern;

$files_array = array();
foreach (glob($pattern) as $f) {
  $spl_file_name = explode("-",$f);
  $to_uid = $spl_file_name[1];
  $from_uid = $spl_file_name[2];
  $timestamp = str_replace(".wav", "", $spl_file_name[3]);
  $files_array[$to_uid][] = array(
    "file_name" => $f,
    "to_user_id" => $to_uid,
    "from_user_id" => $from_uid,
    "timestamp" => (int)$timestamp,
    "timestamp_text" => date("Y-m-d-H:i",(int)$timestamp)
  );
}
//print_r($files_array);
$ret_arr = array();
foreach ($files_array as $fas) {
  //全件
  //$fas_sorted = sortByKey("timestamp", SORT_DESC, $fas);
  //$ret_arr_c = $fas_sorted[0];
  foreach ($fas as $ret_arr_c) {
    // code...
    $latest_file_name = str_replace($dir_path, "", $ret_arr_c["file_name"]);
    $latest_file_name = str_replace(".wav", "", $latest_file_name);
    $f = "../audio_s/{$latest_file_name}.txt";
    if (file_exists($f)) {
      $f_status = @file_get_contents($f);
      $exp_f_status = explode("/",$f_status);
      //再生前だったら
      if ($exp_f_status[0] === "n") {
        $ret_arr_c["is_played_status"] = $exp_f_status[0];
        $ret_arr_c["params"] = $exp_f_status[1];
        $latest_file_name = str_replace($dir_path, "", $ret_arr_c["file_name"]);
        $latest_file_name = str_replace(".wav", "", $latest_file_name);
        $f = "../audio_s/{$latest_file_name}.txt";
        if (file_exists($f)) {
          $f_status = @file_get_contents($f);
          $exp_f_status = explode("/",$f_status);
          $ret_arr_c["is_played_status"] = $exp_f_status[0];
          $ret_arr_c["params"] = $exp_f_status[1];
          $prm_e = explode(",",$exp_f_status[1]);
          //今日中の時日付チェック
          if ($prm_e[0] == "0") {
            if (date("Y/m/d",$ret_arr_c["timestamp"]) !== date("Y/m/d",time())) {
              continue;
            }
          }
          if ($exp_f_status[2] === "last_play_timestamp") {
            $ret_arr_c["least_played_timestamp"] = 0;
          } else {
            $ret_arr_c["least_played_timestamp"] = (int)$exp_f_status[2];
          }
          $tp = "false";
          $w = date("w");
          if ($prm_e[0] === "1") {
            //毎日
            if (date("Y/m/d",$ret_arr_c["least_played_timestamp"]) == date("Y/m/d",time())) {
              $tp = "true";
            }
          } else if ($prm_e[0] === "2") {
            //平日毎日
            if ($w === 0||$w === 6) {
              if (date("Y/m/d",$ret_arr_c["least_played_timestamp"]) == date("Y/m/d",time())) {
                $tp = "true";
              }
            }
          } else if ($prm_e[0] === "3") {
            //休日毎日
            if ($w !== 0||$w !== 6) {
              if (date("Y/m/d",$ret_arr_c["least_played_timestamp"]) == date("Y/m/d",time())) {
                $tp = "true";
              }
            }
          }
          $ret_arr_c["today_played"] = $tp;
        }
        //ユーザ名を取得
        if ($ret_arr_c["to_user_id"] == "1") {
          $to_name = "家族のだれか";
        } else {
          $to_name_json = @json_decode(@file_get_contents("http://tsumugu2626.xyz/nty_login/api/get_family_name.php?familyid={$family_id}&userid={$ret_arr_c["to_user_id"]}"), true);
          $to_name = $to_name_json["familyname"];
        }
        $from_name_json = @json_decode(@file_get_contents("http://tsumugu2626.xyz/nty_login/api/get_family_name.php?familyid={$family_id}&userid={$ret_arr_c["from_user_id"]}"), true);
        $from_name = $from_name_json["familyname"];
        if (empty($to_name)||empty($from_name)) {
          continue;
        }
        $ret_arr_c["to_user_name"] = $to_name;
        $ret_arr_c["from_user_name"] = $from_name;
        $ret_arr_c["file_name_short"] = $latest_file_name;
        $ret_arr[] = $ret_arr_c;
        //
      }
    }
  }
}
if (count($ret_arr) === 0) {
  $status_id = 2;
  $message = "なし";
  $ret_arr = null;
} else {
  $status_id = 1;
  $message = "成功";
}
$js = array(
  "id" => $status_id,
  "message" => $message,
  "message_list" => $ret_arr
);
echo json_encode($js);
