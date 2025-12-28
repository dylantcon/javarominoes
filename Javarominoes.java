/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javarominoes;
import javax.swing.JFrame;

/**
 *
 * @author dylan
 */
public class Javarominoes
{
  public static void main( String[] args )
  {
    JFrame javarominoes = new JFrame( "Javarominoes" );
    javarominoes.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    
    GamePanel gP = new GamePanel();
    javarominoes.add( gP );
    javarominoes.setSize( 640, 480 );
    javarominoes.setVisible( true );
  }
}