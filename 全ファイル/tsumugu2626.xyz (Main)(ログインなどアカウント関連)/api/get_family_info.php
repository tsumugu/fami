<?php
include_once 'dbconnect.php';
//token=userid
$family_id = $_GET["family_id"];
$family_id = str_replace(array(" ","　"),"",$family_id);
$can_func = true;
$message = "";
$status_id = 0;
//存在確認
if (!isset($family_id) || $family_id === '') {
  $can_func = false;
  $message = "エラー";
  $status_id = 3;
}
//DBに登録
if ($can_func) {
  $family_id = $mysqli->real_escape_string($family_id);
  $query = "SELECT * FROM family WHERE family_id = '".$family_id."'";
  $rs = $mysqli->query($query);
  if($rs) {
    $s = "";
    $id = "";
    foreach ($rs as $r) {
      $s = $r["name"];
      $id = $r["family_id"];
    }
    if ($s !== "") {
      $message = array($s, $id);
      $status_id = 1;
    } else {
      $message = "UserNotFound";
      $status_id = 2;
    }
  } else {
    $message = "サーバでエラーが発生しました";
    $status_id = 3;
  }
}
$js = array(
  "id" => $status_id,
  "message" => $message
);
echo json_encode($js);