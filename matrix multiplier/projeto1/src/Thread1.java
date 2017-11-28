//Projeto: Multiplicador Paralelo de Matrizes
//Disciplina: Sistemas Concorrentes - PUCPR
//Alunos: Bruno Cattalini, Lucas Kaniak e Felipe Mathieu
//Setembro, 2017

//Thread Calculadora

import java.util.Vector;
import java.util.concurrent.Semaphore;
 
public class Thread1
  extends Thread
{
  private Semaphore barreira;
  private Semaphore mutex;
  private Contador contador;
  private int n_threads;
  Vector<elemento> respostas;
  
  
  public Thread1(Semaphore barreira, Semaphore mutex, Contador contador, Vector<elemento> elementos, int n)
  {
    this.barreira = barreira;
    this.mutex = mutex;
    this.contador = contador;
    this.respostas = new Vector<elemento>(5,1);
    for(int i = 0; i < elementos.size(); i++)
    {
    	this.respostas.addElement(elementos.elementAt(i));
    }
    this.n_threads = n;
  }
  
  public void run()
  {
	 for(int i = 0; i < respostas.size(); i++)
	 {
		 respostas.get(i).set_resultado();
	 }
	barreira.release();

  } 
}
