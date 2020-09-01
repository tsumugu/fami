<?php
$api_url = "http://weather.livedoor.com/forecast/webservice/json/v1?city=130010";
$json_return = json_decode(@file_get_contents($api_url),true);
$weat = $json_return["forecasts"][0]["telop"];
$is_rain = false;
if (strpos($weat,"雨") !== false) {
  $is_rain = true;
}

$array = array(
    "weather" => $weat,
    "img_url" => "",
    "is_rain" => $is_rain
);
echo @json_encode($array);
