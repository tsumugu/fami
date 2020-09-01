<?php
include_once 'dbconnect.php';
include_once 'url_manager.php';
require_once 'sort_func.php';
//token=userid
$token = $_GET["token"];
$token = str_replace(array(" ","　"),"",$token);
$can_func = true;
$message = "";
$status_id = 0;
$list = array();
//存在確認
if (!isset($token) || $token === '') {
  $can_func = false;
  $message = "tokenエラー";
  $status_id = 2;
}
//userid確認
$query = "SELECT * FROM users";
$unandmct = 0;
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  if ($mail["userid"] == $token) {
    $unandmct++;
  }
}
if ($unandmct == 0) {
  //エラーメッセージ
  $message = "tokenエラー";
  $status_id = 2;
  $can_func = false;
}
//DBに登録
if ($can_func) {
  $token = $mysqli->real_escape_string($token);
  $sql = "SELECT * FROM family";
  $query = $mysqli->query($sql);
  if($query) {
    foreach ($query as $q) {
      if ($q["token"] == $token) {
        $pos = "";
        if ($q["id"] === "0") {
          $pos = "父";
        } else if ($q["id"] === "1") {
          $pos = "母";
        } else if ($q["id"] === "2") {
          $pos = "子";
        } else if ($q["id"] === "3") {
          $pos = "祖父";
        } else if ($q["id"] === "4") {
          $pos = "祖母";
        } else if ($q["id"] === "5") {
          $pos = "その他";
        }
        $f = $token."-".$q["family_id"].".jpeg";
        $f_url = "http://{$vps_url}/nty_img/uploads/".$f;
        $ch = curl_init($f_url);
        curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, true );
        curl_exec($ch);
        if (!curl_errno($ch)) {
          $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
          if ($http_code !== 200) {
            $f = "notfound";
          }
        } else {
          $f = "notfound";
        }
        curl_close($ch);
        //DBから外出帰宅時間取得
        $sql_a = "SELECT * FROM iohistory";
        $query_a = $mysqli->query($sql_a);
        $status = null;
        $timestamp = null;
        if($query_a) {
          $io_history_arr = array();
          foreach ($query_a as $q_a) {
            if ($q_a["userid"] == $q["family_id"] && $q_a["family_id"] == $token) {
              //$status = $q_a["status"];
              //$timestamp = $q_a["timestamp"];
              $io_history_arr[] = array(
                "status" => $q_a["status"],
                "timestamp" => (int)$q_a["timestamp"]
              );
            }
          }
          //timestampが大きい順(降順)に並べる
          $sorted_io_history_arr = sortByKey("timestamp", SORT_DESC, $io_history_arr);
          $status = $sorted_io_history_arr[0]["status"];
          $timestamp = $sorted_io_history_arr[0]["timestamp"];
        }
        if ($status === null || $timestamp === null) {
          $st_str = "--\n--/--/--:--";
          //status = nullだったら
          $status = 0;
        } else {
          if ($status == "0") {
            //外出
            $st_str = "在宅中";
          } else if ($status == "1") {
            $st_str = "外出中";
          } else {
            $st_str = "--";
          }
          $hi = date("d日 H:i",$timestamp);
          $st_str = $st_str."\n".$hi;
        }
        /*
        $arr_a = array();
        $arr2_a = array();
        $db_fid = $q["family_id"];
        if($query_a) {
          foreach ($query_a as $q_a) {
            if ($q_a["userid"] == $token && $q_a["family_id"] == $db_fid) {
              $arr_a[] = $q_a["status"];
              $arr2_a[] = $q_a["timestamp"];
            }
          }
        }
        $arr_a = array_reverse($arr_a);
        $arr2_a = array_reverse($arr2_a);
        $st_str = "";
        if ($arr_a[0] == "0") {
          //外出
          $st_str = "IN";
        } else if ($arr_a[0] == "1") {
          $st_str = "OUT";
        }
        if ($arr2_a[0] != "") {
          $hi = date("H:i",(int)$arr2_a[0]);
          $st_str = $st_str." ".$hi;
        }
        */
        //
        $list[] = array(
          "name" => $q["name"],
          "position" => $pos,
          "family_id" => $q["family_id"],
          "img_url" => $f,
          "io_time" => $st_str,
          "status" => $status
        );
      }
    }
    $message = "取得成功";
    $status_id = 1;
  } else {
    $message = "サーバでエラーが発生しました";
    $status_id = 3;
  }
}
$js = array(
  "id" => $status_id,
  "message" => $message,
  "family_list" => $list
);
echo json_encode($js);
