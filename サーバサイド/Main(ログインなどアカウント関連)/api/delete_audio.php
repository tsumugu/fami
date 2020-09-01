<?php
include_once 'dbconnect.php';
//token=userid
$filename = $_GET["filename"];
$file_path = "../audio_s/".$filename.".txt";
if (file_exists($file_path)) {
  if (@file_put_contents($file_path,"y")) {
    $status_id = 1;
    $message = "成功";
  } else {
    $status_id = 2;
    $message = "削除に失敗しました";
  }
} else {
  $status_id = 2;
  $message = "削除に失敗しました";
}
$js = array(
  "id" => $status_id,
  "message" => $message
);
echo json_encode($js);
