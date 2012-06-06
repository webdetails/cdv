/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cpf;

/**
 *
 * @author pdpi
 */
public class InvalidOperationException extends Exception {

    public InvalidOperationException(Exception parent) {
        super(parent);
    }
}
