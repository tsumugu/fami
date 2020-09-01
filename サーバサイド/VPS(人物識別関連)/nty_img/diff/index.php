<?php
//var_dump($_FILES);
try {
    // 未定義である複数ファイルである$_FILES Corruption 攻撃を受けた
    // どれかに該当していれば不正なパラメータとして処理する
    if (!isset($_FILES['upfile']['error']) || !is_int($_FILES['upfile']['error']) || empty($_POST["family_id"])) {
        throw new RuntimeException('エラーが発生しました[pram]');
    }

    switch ($_FILES['upfile']['error']) {
        case UPLOAD_ERR_OK: // OK
            break;
        case UPLOAD_ERR_NO_FILE:   // ファイル未選択
            throw new RuntimeException('エラーが発生しました[notfound]');
        case UPLOAD_ERR_INI_SIZE:  // php.ini定義の最大サイズ超過
        case UPLOAD_ERR_FORM_SIZE: // フォーム定義の最大サイズ超過 (設定した場合のみ)
            throw new RuntimeException('エラーが発生しました[size]');
        default:
            throw new RuntimeException('エラーが発生しました[other]');
    }
    if (!$ext = array_search(
        mime_content_type($_FILES['upfile']['tmp_name']),
        array(
            'jpeg' => 'image/jpeg'
        ),
        true
    )) {
        throw new RuntimeException('エラーが発生しました[save]');
    }

    // ファイル名を決定 family_id+user_id+face.jpg
    $file_name = $_POST["family_id"]."-".time();

    if (!move_uploaded_file(
        $_FILES['upfile']['tmp_name'],
        $path = sprintf('/var/www/html/nty_img/diff/tmp/%s.%s',
            $file_name,
            $ext
        )
    )) {
        throw new RuntimeException('エラーが発生しました');
    }
    // ファイルのパーミッションを0777に設定
    chmod($path, 0777);

    //顔確認
    exec("/usr/bin/docker run --rm -v /var/www/html:/var/www/html lapidarioz/docker-python-opencv3-dlib python /var/www/html/nty_img/diff/diff_new.py {$path}  2>&1", $output, $return_var);
    //var_dump($output[0]);
    //@file_put_contents("log/".time().".txt",$output[0]);
    $ret_json = @json_decode($output[0],true);
    //@file_put_contents("log/".time().".txt",implode("/",$ret_json["users_face_list"]));
    $face_detect_status = $ret_json["status"];
    if ($return_var !== 0 || $face_detect_status !==  "OK") {
      //NG
      //throw new RuntimeException('エラーが発生しました[python; '.$face_detect_status.']');
      $ret_user_json_list = array();    
    } else {

    $ret_user_json_list = array();
    foreach ($ret_json["users_face_list"] as $list) {
      $token = $list[0];
      if ($token == "unknown") {
        $ret_user_json_list[] = array(
            "name" => "",
            "userid" => "",
            "is_dont_know_people"=>"true",
            "img" => $list[1],
            "io_status"=>"e"
        );
        //不審者アラート
        $url = "http://tsumugu2626.xyz/nty_login/api/notice_unknown.php?family_id=".$_POST["family_id"];
        @file_get_contents($url);
      } else {
        //notice APIにPOST      
        $url = "http://tsumugu2626.xyz/nty_login/api/notice.php?family_id=".$_POST["family_id"]."&token=".$token;
        $notice_ret = @file_get_contents($url);
        $json_dec_notice_ret = @json_decode($notice_ret,true);
        $io_status = $json_dec_notice_ret["io_status"];
        if ($io_status == null) {
          $io_status = "e";
        }
        $username_json = @json_decode(@file_get_contents("http://tsumugu2626.xyz/nty_login/api/get_family_name.php?familyid=".$_POST["family_id"]."&userid=".$token),true);
        //@file_put_contents("log/".time().".txt","http://tsumugu2626.xyz/nty_login/api/get_family_name.php?familyid=".$_POST["family_id"]."&userid=".$token);
        if ($username_json["id"] === 1) {
          $ret_user_json_list[] = array(
            "name" => $username_json["familyname"],
            "userid" => $token,
            "is_dont_know_people"=>"false",
            "io_status"=>$io_status
          );
        }
      }
    }
}
    echo json_encode(array(
      "id" => 1,
      "message" => "upload succeed",
      "user_list" => $ret_user_json_list
    ));

} catch (RuntimeException $e) {
  //アプリ側のバグによりstrでエラーコードを返す
  echo json_encode(array(
    "id" => "2",
    "message" => $e->getMessage()
  ));
}
