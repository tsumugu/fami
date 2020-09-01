<?php
include_once 'dbconnect.php';
require_once 'sort_func.php';
$userid = $_GET["user_id"];
$familyid = $_GET["family_id"];
$can_regist = true;
$message = "";
$status_id = 0;
//存在確認
if (!isset($userid) || $userid === '' || !isset($familyid) || $familyid === '') {
  $can_regist = false;
  $message = "正しく入力してください";
  $status_id = 2;
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
if ($unandmct == 0) {
  $message = "正しく入力してください";
  $status_id = 2;
} else {
  //入退出履歴
  $sql_a = "SELECT * FROM iohistory";
  $query_a = $mysqli->query($sql_a);
  $arr_a = array();
  if($query_a) {
    foreach ($query_a as $q_a) {
      if ($q_a["userid"] == $userid && $q_a["family_id"] == $familyid) {
        $status = $q_a["status"];
        if ($status == "0") {
          //外出
          $st_str = "帰宅";
        } else if ($status == "1") {
          $st_str = "外出";
        } else {
          $st_str = "--";
        }
        $arr_a[] = array(
          "status" => $q_a["status"],
          "timestamp" => (int)$q_a["timestamp"],
          "status_text" => $st_str,
          "timestamp_text" => date("Y/m/d H:i",(int)$q_a["timestamp"])
        );
      }
    }
  }
  //$arr_a = array_reverse($arr_a);
  $arr_a = sortByKey("timestamp", SORT_DESC, $arr_a);
  $status_id = 1;
  $message = "OK";
}
$js = array(
  "id" => $status_id,
  "message" => $message,
  "io_history_list" => $arr_a
);
echo json_encode($js);
