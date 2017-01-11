<?php


$uname = $_POST['uname'];
$count = $_POST['filesize'];
//$paramValue = $_POST['value1'];
for($i = 0;$i<$count;$i++)
{

$type = $_FILES["file".$i]["type"];
$name = $_FILES["file".$i]["name"];	
$new_image_name = $name;
move_uploaded_file($_FILES["file".$i]["tmp_name"], "../uploads/".$new_image_name);
}

echo $uname;
?>