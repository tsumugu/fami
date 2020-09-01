<?php
include_once 'dbconnect.php';

$is_post_error = false;
$message = "";
$status_id = 0;
$token = "";
if (!isset($_GET['email']) || $_GET['email'] === '' || !isset($_GET['password']) || $_GET['password'] === '') {
  $is_post_error = true;
}
if ($is_post_error) {
  $message = "メールアドレスとパスワードを正しく入力してください";
  $status_id = 2;
} else {
  $email = $mysqli->real_escape_string($_GET['email']);
  //$password = $mysqli->real_escape_string($_GET['password']);
  $password = $_GET['password'];
  // emailの確認
  $query = "SELECT * FROM users WHERE email='$email'";
  $result = $mysqli->query($query);
  if ($result) {
    // パスワードの取り出し
    $db_hashed_pwd = "";
    $db_userid = "";
    while ($row = $result->fetch_assoc()) {
      $db_hashed_pwd = $row['password'];
      $db_userid = $row['userid'];
      $db_is_formal_registration = $row['is_formal_registration'];
    }
    // データベースの切断
    $result->close();
    // ハッシュ化されたパスワードがマッチするかどうかを確認
    if (password_verify($password, $db_hashed_pwd)) {
      //TODO: メール届かない問題
      /*
      if ($db_is_formal_registration === "true") {
        $message = "ログインに成功しました";
        $token = $db_userid;
        $status_id = 1;
      } else {
        $message = "メールのリンクを開いて確認を完了させてください";
        $status_id = 3;
      }
      */
      $message = "ログインに成功しました";
      $token = $db_userid;
      $status_id = 1;
    } else {
      $message = "メールアドレスかパスワードが間違っています";
      $status_id = 3;
    }
  } else {
    $message = "サーバでエラーが発生しました";
    $status_id = 4;
    $mysqli->close();
  }
}
$js = array(
  "id" => $status_id,
  "message" => $message,
  "token" => $token
);
echo json_encode($js);
