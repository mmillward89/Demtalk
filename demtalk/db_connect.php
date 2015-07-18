<?php
 
/**
 * A class file to connect to database
 */
class DB_CONNECT {
 
    private static instance = null;
    private $db;
    
    // constructor
    private function __construct() {
        
        require_once __DIR__ . '/db_config.php';
        
        $options = array(PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_OBJ, PDO::ATTR_ERRMODE => PDO::ERRMODE_WARNING);
 
        // Connecting to mysql database
        try {
        $db = new PDO('mysql:host=localhost;dbname=DemTalk;charset=utf8', DB_USER, DB_PASSWORD, $options);
        }
        catch(PDOException $e) {
				echo "Connection error." . $e->getMessage();
			}
    }
    
    public static function getInstance() {
			
			// check if instance has not yet been instantiated first
			if(!isset(self::$_instance)) {
				self::$_instance = new db_connect();
			}
			
			return self::$_instance;
			
		}
 
    // destructor
    function __destruct() {
        // closing db connection
        $this->close();
    }

    /**
     * Function to close db connection
     */
    function close() {
        // closing db connection
        mysql_close();
    }
 
}
 
?>