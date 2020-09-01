<?php
include_once 'dbconnect.php';
//token=userid
$userid = $_POST["user_id"];
$from_userid = $_POST["from_user_id"];
$familyid = $_POST["family_id"];
$params = $_POST["param"];
$can_regist = true;
$message = "";
$status_id = 0;
//存在確認
if (!isset($userid) || $userid === '' || !isset($familyid) || $familyid === '' || !isset($from_userid) || $from_userid === '') {
  $can_regist = false;
  $message = "正しく入力してください";
  $status_id = 2;
}
//userid確認
$query = "SELECT * FROM users";
$unandmct = 0;
$mails = $mysqli->query($query);
foreach ($mails as $mail) {
  if ($mail["userid"] == $userid) {
    $unandmct++;
  }
}
if ($unandmct == 0) {
  //エラーメッセージ
  $message = "tokenエラー";
  $status_id = 3;
  $can_regist = false;
}
if ($can_regist) {
  try {
    if (!isset($_FILES['upfile']['error']) || !is_int($_FILES['upfile']['error'])) {
      throw new RuntimeException('パラメータが不正です');
    }
    switch ($_FILES['upfile']['error']) {
        case UPLOAD_ERR_OK:
            break;
        case UPLOAD_ERR_NO_FILE:
            throw new RuntimeException('ファイルが選択されていません');
        case UPLOAD_ERR_INI_SIZE:
        case UPLOAD_ERR_FORM_SIZE:
            throw new RuntimeException('ファイルサイズが大きすぎます');
        default:
            throw new RuntimeException('その他のエラーが発生しました');
    }
    if (!$ext = array_search(
        mime_content_type($_FILES['upfile']['tmp_name']),
        array(
            'wav' => 'audio/wav',
            'wav' => 'audio/x-wav',
            'wav' => 'video/3gpp'
        ),
        true
    )) {
        throw new RuntimeException('ファイル形式が不正です');
    }
    $filename = $userid."-".$familyid."-".$from_userid."-".time();
    if (!move_uploaded_file(
        $_FILES['upfile']['tmp_name'],
        $path = sprintf('../up_audio/%s.%s',
            $filename,
            $ext
        )
    )) {
        throw new RuntimeException('ファイル保存時にエラーが発生しました');
    }
    //
    $message = "OK";
    $status_id = 1;
    $st_txt = "../audio_s/".$filename.".txt";
    @file_put_contents($st_txt,"n/".$params."/last_play_timestamp");
} catch (RuntimeException $e) {
    $message = $e->getMessage();
    $status_id = 4;
}
}
$js = array(
  "id" => $status_id,
  "message" => $message
);
echo json_encode($js);
