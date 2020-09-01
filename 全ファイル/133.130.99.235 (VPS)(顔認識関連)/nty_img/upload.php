<?php
//var_dump($_FILES);
try {
    // 未定義である複数ファイルである$_FILES Corruption 攻撃を受けた
    // どれかに該当していれば不正なパラメータとして処理する
    if (!isset($_FILES['upfile']['error']) || !is_int($_FILES['upfile']['error']) || empty($_POST["family_id"]) || empty($_POST["user_id"])) {
        throw new RuntimeException('エラーが発生しました');
    }

    switch ($_FILES['upfile']['error']) {
        case UPLOAD_ERR_OK: // OK
            break;
        case UPLOAD_ERR_NO_FILE:   // ファイル未選択
            throw new RuntimeException('エラーが発生しました');
        case UPLOAD_ERR_INI_SIZE:  // php.ini定義の最大サイズ超過
        case UPLOAD_ERR_FORM_SIZE: // フォーム定義の最大サイズ超過 (設定した場合のみ)
            throw new RuntimeException('エラーが発生しました');
        default:
            throw new RuntimeException('エラーが発生しました');
    }
    if (!$ext = array_search(
        mime_content_type($_FILES['upfile']['tmp_name']),
        array(
            'jpeg' => 'image/jpeg'
        ),
        true
    )) {
        throw new RuntimeException('エラーが発生しました');
    }

    // ファイル名を決定 family_id+user_id+face.jpg
    $file_name = $_POST["user_id"]."-".$_POST["family_id"]."-".time();

    if (!move_uploaded_file(
        $_FILES['upfile']['tmp_name'],
        $path = sprintf('/var/www/html/nty_img/tmp/%s.%s',
            $file_name,
            $ext
        )
    )) {
        throw new RuntimeException('エラーが発生しました');
    }
    // ファイルのパーミッションを0777に設定
    chmod($path, 0777);

    //顔確認
    exec("/usr/bin/docker run --rm -v /var/www/html:/var/www/html jjanzic/docker-python3-opencv python /var/www/html/nty_img/check_face_exist.py {$path}  2>&1", $output, $return_var);
    $ret_json = @json_decode($output[0],true);
    $face_detect_status = $ret_json["status"];
    if ($return_var !== 0 || $face_detect_status !==  "OK") {
      //NG
      throw new RuntimeException('顔が検出出来ませんでした');
    }

    echo json_encode(array(
      "id" => 1,
      "message" => "upload succeed",
      "face_list" => $ret_json["faces_path"]
    ));

} catch (RuntimeException $e) {
  echo json_encode(array(
    "id" => 2,
    "message" => $e->getMessage()
  ));
}
