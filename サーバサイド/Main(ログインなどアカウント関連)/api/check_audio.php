 <?php
include_once 'dbconnect.php';
function check_params($params, $created_timestamp, $least_played_timestamp, $io_status) {
  $now_timestamp = time();
  //日曜日: 0
  $w = date("w");
  $w = (int)$w;
  $prms_exp = explode(",",$params);
  $prm_date = $prms_exp[0];
  $prm_opt = $prms_exp[1];
  $prm_timing = $prms_exp[3];
  //0. 今日はまだ再生されていないか
  if (date("Y/m/d",$least_played_timestamp) === date("Y/m/d",$now_timestamp)) {
    return false;
  }
  //1. 日付を確認
  if ($prm_date === "0") {
    //今日のとき→timestampを確認
    if (date("Y/m/d",$created_timestamp) !== date("Y/m/d",$now_timestamp)) {
      return false;
    }
  } else if ($prm_date === "2") {
    //平日のとき
    if ($w === 0||$w === 6) {
      return false;
    }
  } else if ($prm_date === "3") {
    //休日のとき
    if ($w !== 0||$w !== 6) {
      return false;
    }
  } else if ($prm_date === "4" || $prm_date === "5" || $prm_date === "6" || $prm_date === "7" || $prm_date === "8" || $prm_date === "9" || $prm_date === "10") {
    if ($prm_date === "4") {
      //月曜日のとき
      if ($w !== 1) {
        return false;
      }
    } else if ($prm_date === "5") {
      //火曜日のとき
      if ($w !== 2) {
        return false;
      }
    } else if ($prm_date === "6") {
      //水曜日のとき
      if ($w !== 3) {
        return false;
      }
    } else if ($prm_date === "7") {
      //木曜日のとき
      if ($w !== 4) {
        return false;
      }
    } else if ($prm_date === "8") {
      //金曜日のとき
      if ($w !== 5) {
        return false;
      }
    } else if ($prm_date === "9") {
      //土曜日のとき
      if ($w !== 6) {
        return false;
      }
    } else if ($prm_date === "10") {
      //日曜日のとき
      if ($w !== 0) {
        return false;
      }
    }
  }
  //2. オプション(雨が降っているか)
  if ($prm_opt === "1") {
    //あめふってるか
    $weat_json_arr = @json_decode(@file_get_contents("http://tsumugu2626.xyz/nty_login/api/get_weather.php"),true);
    if (empty($weat_json_arr) || $weat_json_arr["is_rain"] === false) {
      return false;
    }
  }
  //3. タイミング
  if ($prm_timing == "1") {
    //外出時
    if ($io_status !== "1") {
      return false;
    }
  } else if ($prm_timing == "2") {
    //帰宅時
    if ($io_status !== "0") {
      return false;
    }
  }
  //ぜんぶOKだったらreturn true
  return true;
}
//token=userid
$userid = $_GET["user_id"];
$familyid = $_GET["family_id"];
$io_status = $_GET["io_status"];
$can_regist = true;
$message = "";
$status_id = 0;
//存在確認
if (!isset($userid) || $userid === '' || !isset($familyid) || $familyid === '') {
  $can_regist = false;
  $message = "正しく入力してください";
  $status_id = 2;
  $dl_file_name_list = null;
}
//userid確認
$query = "SELECT * FROM users";
$unandmct = 0;
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  if ($mail["userid"] == $familyid) {
    $unandmct++;
  }
}
$dl_file_name_list = array();
if ($unandmct == 0) {
  $message = "正しく入力してください";
  $status_id = 2;
  $dl_file_name_list = null;
} else {
  $get_all_latest_audio_arr = @file_get_contents("http://tsumugu2626.xyz/nty_login/api/get_all_latest_audio.php?family_id={$familyid}");
  $json_decode = @json_decode($get_all_latest_audio_arr, true);
  $mes_list = $json_decode["message_list"];
  if (!empty($mes_list)) {
    foreach ($json_decode["message_list"] as $mes) {
      if ($mes["to_user_id"] == "1" || $mes["to_user_id"] == $userid) {
        //paramを解析する
        $params = $mes["params"];
        //チェック
        if (check_params($params, $mes["timestamp"], $mes["least_played_timestamp"], $io_status)) {
          $dl_file_name_list[] = array("file_name"=>$mes["file_name_short"]);
          //再生済みにする
          $prms_exp = explode(",",$params);
          $prm_date = $prms_exp[0];
          $st_txt = "../audio_s/".$mes["file_name_short"].".txt";
          if ($prm_date === "1" || $prm_date === "2" || $prm_date === "3") {
            $w_text = "n/".$params."/".time();
          } else if ($prm_date === "0" || $prm_date === "4" || $prm_date === "5" || $prm_date === "6" || $prm_date === "7" || $prm_date === "8" || $prm_date === "9" || $prm_date === "10") {
            $w_text = "y";
          }
          @file_put_contents($st_txt, $w_text);
        }
      }
    }
    $message = "y";
    $status_id = 1;
  } else {
    $message = "n";
    $status_id = 1;
    $dl_file_name_list = null;
  }
}
$js = array(
  "id" => $status_id,
  "message" => $message,
  "download_file_name_list" => $dl_file_name_list
);
echo json_encode($js);
