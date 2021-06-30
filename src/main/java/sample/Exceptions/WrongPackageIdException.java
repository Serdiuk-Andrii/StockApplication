/*This file defines an exception thrown when the server receives incorrect package id.
  File: WrongPackageIdException.java
  Author: Serdiuk Andrii
 */

package Exceptions;

public class WrongPackageIdException extends Exception{

    public WrongPackageIdException(String reason) {
        super(reason);
    }

}
