/*This file defines an exception which may occur during encryption and decryption processes.
  File: Exceptions.CryptoException.java
  Author: Serdiuk Andrii
 */

package Exceptions;

public class CryptoException extends Exception{

    public CryptoException(String reason) {
        super(reason);
    }

}
