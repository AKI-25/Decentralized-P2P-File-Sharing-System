package pepClient;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import p2pClientServer.p2pClientAsServer;

public class p2pClient implements Runnable {
	static String key;
	static String value;
	static int index;
	static int server_port;
	static String host_ip;
	static String dirname = null;
	ServerSocket serverSocket = null;
	Socket serverConnectionSocket = null;
	static Map<Integer, String> config = new LinkedHashMap<Integer, String>();
	static Map<Integer,String> config1 = null;
	static ArrayList<String> fileNames = new ArrayList<String>();
	static ArrayList<String> peerList = new ArrayList<String>();
	static List<String> peer_list1 = new ArrayList<String>();
	static Random randomGenerator = new Random();
	static Socket[] socketArray= new Socket[8]; 
	public p2pClient(int Port_no){
		try {
			serverSocket = new ServerSocket(Port_no);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("le numero du port de l'hote :"+serverSocket.getLocalPort());
		new Thread(this).start();
	}
	
	
	public static void main(String args[]) throws Exception{
		
		System.out.println("Entrer le port du serveur: ");
		Scanner sc1 = new Scanner(System.in);
		server_port = sc1.nextInt();
		System.out.println("Enter l'addresse IP du serveur: ");
		Scanner sc11 = new Scanner(System.in);
		host_ip = sc11.nextLine();
		p2pClient client1 = new p2pClient(server_port);
		client1.getConfigValues();
		clientOpetions();
		
	}
	public static void clientOpetions() throws Exception{
		Socket getSocket = new Socket(host_ip,server_port);
		config1 = new TreeMap<Integer,String>(config);
		System.out.println(config1);
		Iterator it = config1.entrySet().iterator();
		int length = config1.size();
		String[] hostInfo = null;
		int i =0;
		
		while (it.hasNext())
		{
			Map.Entry pair = (Map.Entry) it.next();
			String hostAddress = pair.getValue().toString();
			hostInfo = hostAddress.split(":");
			int allPort = Integer.parseInt(hostInfo[1]);
			String allHostIp = hostInfo[0];
			System.out.println("ID : "+pair.getKey().toString()+" Informations sur l'hote : "+pair.getValue().toString()+" IP de l'hote : "+allHostIp+" Port : "+allPort);
			peerList.add(allHostIp+":"+allPort);
			if(allPort == server_port)
			{
				socketArray[i]= getSocket;
			}
			else
			{
				boolean scanning = true;
				while(scanning)
				{
					try
					{
						socketArray[i] = new Socket(allHostIp,allPort);
						scanning = false;
					}
					catch(ConnectException e) {
						System.out.println("Connection echouee, les etudiants ne sont pas tous connecte entre eux.\n En attente d'un nouvelle connection d'un etudiant...... \n Tentative de connection dans 20 secondes..............");
						try
						{
							Thread.sleep(20000);
						}
						catch(Exception ex) {
						}
					}
				}	
			}
			i++;
		}
		System.out.println(i);
		ObjectOutputStream obj1 = new ObjectOutputStream(socketArray[0].getOutputStream());
		ObjectOutputStream obj2 = new ObjectOutputStream(socketArray[1].getOutputStream());
		ObjectOutputStream obj3 = new ObjectOutputStream(socketArray[2].getOutputStream());
		ObjectOutputStream obj4 = new ObjectOutputStream(socketArray[3].getOutputStream());
		ObjectOutputStream obj5 = new ObjectOutputStream(socketArray[4].getOutputStream());
		ObjectOutputStream obj6 = new ObjectOutputStream(socketArray[5].getOutputStream());
		ObjectOutputStream obj7 = new ObjectOutputStream(socketArray[6].getOutputStream());
		ObjectOutputStream obj8 = new ObjectOutputStream(socketArray[7].getOutputStream());
		

		try {
			Thread.sleep(10000);
		} catch(Exception e) {
		}
		try {
		int randHost = 0;
        String replicationHost= null;
		try {
			System.out.println("Veuillez entrer le nom du repertoire partage : ");
			Scanner sc1 = new Scanner(System.in);
			dirname = sc1.nextLine();		
			File dir = new File(dirname);
			if (!dir.exists()) {
				System.out.println(" le repertoire partage n'existe pas ! .....Creation d'un nouveau repertoire partage "+dir);
				dir.mkdir();
			}
			
			//Decide Replication Host
			boolean abc = true;
			while(abc)
			{
				randHost = randomGenerator.nextInt(config1.size());
				//replicationHost= peerList.get(randHost);
				randHost = randHost+1;
				replicationHost = config1.get(randHost);
				System.out.println("Valeur : "+replicationHost);
				String[] storeArray = null;
				storeArray = replicationHost.split(":");
				String hostIp = storeArray[0];
				int hostPort = Integer.parseInt(storeArray[1]);
		        if(server_port!=hostPort)
		        {
		        	abc = false;
		        }
			}
	        System.out.println("Hote-Alea : "+randHost+" Hote de replication : "+replicationHost);
			
	        //Register All files in Shared Directory
	        String file_name = null;
			getPeerFiles(dir);
			final long registerStartTie = System.nanoTime();
			final long registerEndTime;
			for(String s1:fileNames){
	        	//System.out.println(s1);
	        	file_name = s1;
	        	int indexServer = hashFunction(s1);
	        	
				if(indexServer==1){
					//System.out.println("Server-1");
					registerFile(obj1,replicationHost,file_name,0);
				}
				else if(indexServer==2){
					//System.out.println("Server-2");
					registerFile(obj2,replicationHost,file_name,1);
				}
				else if(indexServer==3){
					//System.out.println("Server-3");
					registerFile(obj3,replicationHost,file_name,2);
				}
				else if(indexServer==4){
					//System.out.println("Server-4");
					registerFile(obj4,replicationHost,file_name,3);
				}
				else if(indexServer==5){
					//System.out.println("Server-5");
					registerFile(obj5,replicationHost,file_name,4);
				}
				else if(indexServer==6){
					//System.out.println("Server-6");
					registerFile(obj6,replicationHost,file_name,5);
				}
				else if(indexServer==7){
					//System.out.println("Server-7");
					registerFile(obj7,replicationHost,file_name,6);
				}
				else if(indexServer==0){
					//System.out.println("Server-8");
					registerFile(obj8,replicationHost,file_name,7);
				}
			//}
//			registerEndTime = System.nanoTime();
//	        final long registerDuration = registerEndTime
//					- registerStartTie;
//			System.out.println("Register Response time: "
//					+ registerDuration + "ns");
//			//fileNames.clear();
//			for(String s2:fileNames){
//	        	//System.out.println(s1);
//	        	file_name = s2;
				//Replicate Files into another peer - RelicationHost
				if(randHost==1){
					//System.out.println("8081");
					String repdirname = "etudiant1_rep";
					replecateFile(obj1,file_name, repdirname);
				}
				else if(randHost==2){
					//System.out.println("8082");
					String repdirname = "etudiant2_rep";
					replecateFile(obj2,file_name, repdirname);
				}
				else if(randHost==3){
					//System.out.println("8083");
					String repdirname = "etudiant3_rep";
					replecateFile(obj3,file_name, repdirname);
				}
				else if(randHost==4){
					//System.out.println("8084");
					String repdirname = "etudiant4_rep";
					replecateFile(obj4,file_name, repdirname);
				}
				else if(randHost==5){
					//System.out.println("8085");
					String repdirname = "etudiant5_rep";
					replecateFile(obj5,file_name, repdirname);
				}
				else if(randHost==6){
					//System.out.println("8086");
					String repdirname = "etudiant6_rep";
					replecateFile(obj6,file_name, repdirname);
				}
				else if(randHost==7){
					//System.out.println("8087");
					String repdirname = "etudiant7_rep";
					replecateFile(obj7,file_name, repdirname);
				}
				else if(randHost==8){
					//System.out.println("8089");
					String repdirname = "etudiant8_rep";
					replecateFile(obj8,file_name, repdirname);
				}
	        }
			registerEndTime = System.nanoTime();
	        final long registerDuration = registerEndTime
					- registerStartTie;
			System.out.println("Register Response time: "
					+ registerDuration + "ns");
			//fileNames.clear();
		}
		catch(Exception e)
		{
			System.out.println("Directory is empty");
		}
		
		while(true){
			int choice = 0;
			System.out.println("Etudiant : "+server_port);
			System.out.println("Options: ");
			System.out.println("1 - Ajouter un fichier ");
			System.out.println("2 - Rechcercher un fichier ");
			System.out.println("3 - Telecharger un fichier ");
			System.out.println("4 - Quitter");
			Scanner sc = new Scanner(System.in);
			try{
				choice = sc.nextInt();
			}
			catch(InputMismatchException e){
				System.out.println("Entree doit etre un entier.....");
				clientOpetions();
			}
			switch(choice){
			case 1: 
				String addFile;
				System.out.println("Entrer le nom du fichier : ");
				Scanner scf = new Scanner(System.in);
				addFile = scf.nextLine();
				final long registerStartTime1 = System.nanoTime();
				final long registerEndTime1;
				int getServerIndex = hashFunction(addFile);
				if(getServerIndex==1){
					System.out.println("Serveur-1");
					registerFile(obj1,replicationHost,addFile,0);
					//break;
				}
				else if(getServerIndex==2){
					System.out.println("Serveur-2");
					registerFile(obj2,replicationHost,addFile,1);
					//break;
				}
				else if(getServerIndex==3){
					System.out.println("Serveur-3");
					registerFile(obj3,replicationHost,addFile,2);
					//break;
				}
				else if(getServerIndex==4){
					System.out.println("Serveur-4");
					registerFile(obj4,replicationHost,addFile,3);
					//break;
				}
				if(getServerIndex==5){
					System.out.println("Serveur-5");
					registerFile(obj5,replicationHost,addFile,4);
					//break;
				}
				else if(getServerIndex==6){
					System.out.println("Serveur-6");
					registerFile(obj6,replicationHost,addFile,5);
					//break;
				}
				else if(getServerIndex==7){
					System.out.println("Serveur-7");
					registerFile(obj7,replicationHost,addFile,6);
					//break;
				}
				else if(getServerIndex==0){
					System.out.println("Serveur-8");
					registerFile(obj8,replicationHost,addFile,7);
					//break;
				}
				else{
					System.out.println("le fichier souhaite n'existe pas");
					break;
				}
				if(randHost==1){
					System.out.println("8081");
					String repdirname = "etudiant1_rep";
					replecateFile(obj1,addFile, repdirname);
					//break;
				}
				else if(randHost==2){
					System.out.println("8082");
					String repdirname = "etudiant2_rep";
					replecateFile(obj2,addFile, repdirname);
					//break;
				}
				else if(randHost==3){
					System.out.println("8083");
					String repdirname = "etudiant3_rep";
					replecateFile(obj3,addFile, repdirname);
					//break;
				}
				else if(randHost==4){
					System.out.println("8084");
					String repdirname = "etudiant4_rep";
					replecateFile(obj4,addFile, repdirname);
					//break;
				}
				else if(randHost==5){
					System.out.println("8085");
					String repdirname = "etudiant5_rep";
					replecateFile(obj5,addFile, repdirname);
					//break;
				}
				else if(randHost==6){
					System.out.println("8086");
					String repdirname = "etudiant6_rep";
					replecateFile(obj6,addFile, repdirname);
					//break;
				}
				else if(randHost==7){
					System.out.println("8087");
					String repdirname = "etudiant7_rep";
					replecateFile(obj7,addFile, repdirname);
					//break;
				}
				else if(randHost==8){
					System.out.println("8089");
					String repdirname = "etudiant8_rep";
					replecateFile(obj8,addFile, repdirname);
					//break;
				}
			    else{
					System.out.println("Replication du repertoire n'est pas possible");
					//break;
				}
				registerEndTime1 = System.nanoTime();
		        final long registerDuration1 = registerEndTime1
						- registerStartTime1;
				System.out.println("le delai du reponse pour un seul fichier est: "
						+ registerDuration1 + "ns");
				break;
			case 2 : 	
				String searchFile;
				Scanner sc2= new Scanner(System.in);
				System.out.println("Entrer le nom du fichier souhaite : ");
				searchFile = sc2.nextLine();
				final long registerStartTime2 = System.nanoTime();
				final long registerEndTime2;
				int getIndex = hashFunction(searchFile);
				if(getIndex==1){
					System.out.println("Serveur-1");
					searchFile(obj1,searchFile,0);
					//break;
				}
				else if(getIndex==2){
					System.out.println("Serveur-2");
					searchFile(obj2,searchFile,1);
					//break;
				}
				else if(getIndex==3){
					System.out.println("Serveur-3");
					searchFile(obj3,searchFile,2);
					//break;
				}
				else if(getIndex==4){
					System.out.println("Serveur-4");
					searchFile(obj4,searchFile,3);
					//break;
				}
				else if(getIndex==5){
					System.out.println("Serveur-5");
					searchFile(obj5,searchFile,4);
					//break;
				}
				else if(getIndex==6){
					System.out.println("Serveur-6");
					searchFile(obj6,searchFile,5);
					//break;
				}
				else if(getIndex==7){
					System.out.println("Serveur-7");
					searchFile(obj7,searchFile,6);
					//break;
				}
				else if(getIndex==0){
					System.out.println("Serveur-8");
					searchFile(obj8,searchFile,7);
					//break;
				}
				else{
					System.out.println("le cle entre est invalid");
					//break;
				}
				registerEndTime2 = System.nanoTime();
		        final long registerDuration2 = registerEndTime2
						- registerStartTime2;
				System.out.println("le delai du reponse pour un seul fichier est: "
						+ registerDuration2 + "ns");
				break;
				
			case 3:
				String obtainFileName;
				Scanner sc3= new Scanner(System.in);
				System.out.println("Entrer le nom du fichier que vous voulez telecharger : ");
				obtainFileName = sc3.nextLine();
				final long registerStartTime3 = System.nanoTime();
				final long registerEndTime3;
				int getpeerIndex = hashFunction(obtainFileName);
				if(getpeerIndex==1){
					System.out.println("Serveur-1");
					searchFile(obj1,obtainFileName,0);
					System.out.println("Liste des etudiants :");
					int[] obtainHost = returnKeys();
					for(int j = 0; j<obtainHost.length; j++)
					{
						System.out.println("Recuperer l'hote : "+obtainHost[j]);
						try {
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8",7); }
							break;
						} catch (Exception e) {
							System.out.println("L'hote est introuvable..Telechargement du fichier d'un autre hote ");
							j++;
							System.out.println("Recuperer l'hote : "+obtainHost[j]);
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1_rep",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2_rep",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3_rep",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4_rep",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5_rep",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6_rep",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7_rep",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8_rep",7); }
						}
					}
					//break;
				}
				else if(getpeerIndex==2){
					System.out.println("Serveur-2");
					searchFile(obj2,obtainFileName,1);
					System.out.println("Liste des etudiants :");
					int[] obtainHost = returnKeys();
					for(int j = 0; j<obtainHost.length; j++)
					{
						System.out.println("Recuperer l'hote : "+obtainHost[j]);
						try {
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8",7); }
							break;
						} catch (Exception e) {
							System.out.println("L'hote est introuvable..Telechargement du fichier d'un autre hote  ");
							j++;
							System.out.println("Recuperer l'hote : "+obtainHost[j]);
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1_rep",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2_rep",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3_rep",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4_rep",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5_rep",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6_rep",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7_rep",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8_rep",7); }
						}
					}
					//break;
				}
				else if(getpeerIndex==3){
					System.out.println("Serveur-3");
					searchFile(obj3,obtainFileName,2);
					System.out.println("Liste des etudiants :");
					int[] obtainHost = returnKeys();
					for(int j = 0; j<obtainHost.length; j++)
					{
						System.out.println("Recuperer l'hote : "+obtainHost[j]);
						try {
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8",7); }
							break;
						} catch (Exception e) {
							System.out.println("L'hote est introuvable..Telechargement du fichier d'un autre hote  ");
							j++;
							System.out.println("Recuperer l'hote : "+obtainHost[j]);
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1_rep",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2_rep",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3_rep",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4_rep",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5_rep",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6_rep",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7_rep",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8_rep",7); }
						}
					}
					//break;
				}
				else if(getpeerIndex==4){
					System.out.println("Serveur-4");
					searchFile(obj4,obtainFileName,3);
					System.out.println("Liste des etudiants :");
					int[] obtainHost = returnKeys();
					for(int j = 0; j<obtainHost.length; j++)
					{
						System.out.println("Recuperer l'hote : "+obtainHost[j]);
						try {
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8",7); }
							break;
						} catch (Exception e) {
							System.out.println("L'hote est introuvable..Telechargement du fichier d'un autre hote  ");
							j++;
							System.out.println("Recuperer l'hote : "+obtainHost[j]);
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1_rep",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2_rep",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3_rep",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4_rep",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5_rep",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6_rep",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7_rep",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8_rep",7); }
						}
					}
					//break;
				}
				else if(getpeerIndex==5){
					System.out.println("Serveur-5");
					searchFile(obj5,obtainFileName,4);
					System.out.println("Liste des etudiants :");
					int[] obtainHost = returnKeys();
					for(int j = 0; j<obtainHost.length; j++)
					{
						System.out.println("Recuperer l'hote : "+obtainHost[j]);
						try {
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8",7); }
							break;
						} catch (Exception e) {
							System.out.println("L'hote est introuvable..Telechargement du fichier d'un autre hote  ");
							j++;
							System.out.println("Recuperer l'hote : "+obtainHost[j]);
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1_rep",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2_rep",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3_rep",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4_rep",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5_rep",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6_rep",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7_rep",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8_rep",7); }
						}
					}
					//break;
				}
				else if(getpeerIndex==6){
					System.out.println("Serveur-6");
					searchFile(obj6,obtainFileName,5);
					System.out.println("Liste des etudiants :");
					int[] obtainHost = returnKeys();
					for(int j = 0; j<obtainHost.length; j++)
					{
						System.out.println("Recuperer l'hote : "+obtainHost[j]);
						try {
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8",7); }
							break;
						} catch (Exception e) {
							System.out.println("L'hote est introuvable..Telechargement du fichier d'un autre hote  ");
							j++;
							System.out.println("Recuperer l'hote : "+obtainHost[j]);
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1_rep",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2_rep",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3_rep",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4_rep",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5_rep",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6_rep",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7_rep",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8_rep",7); }
						}
					}
					//break;
				}
				else if(getpeerIndex==7){
					System.out.println("Serveur-7");
					searchFile(obj7,obtainFileName,6);
					System.out.println("Liste des etudiants :");
					int[] obtainHost = returnKeys();
					for(int j = 0; j<obtainHost.length; j++)
					{
						System.out.println("Recuperer l'hote : "+obtainHost[j]);
						try {
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8",7); }
							break;
						} catch (Exception e) {
							System.out.println("L'hote est introuvable..Telechargement du fichier d'un autre hote  ");
							j++;
							System.out.println("Recuperer l'hote : "+obtainHost[j]);
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1_rep",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2_rep",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3_rep",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4_rep",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5_rep",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6_rep",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7_rep",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8_rep",7); }
						}
					}
					//break;
				}
				else if(getpeerIndex==0){
					System.out.println("Serveur-8");
					searchFile(obj8,obtainFileName,7);
					System.out.println("Liste des etudiants :");
					int[] obtainHost = returnKeys();
					for(int j = 0; j<obtainHost.length; j++)
					{
						System.out.println("Recuperer l'hote : "+obtainHost[j]);
						try {
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8",7); }
							break;
						} catch (Exception e) {
							System.out.println("L'hote est introuvable..Telechargement du fichier d'un autre hote  ");
							j++;
							System.out.println("Recuperer l'hote : "+obtainHost[j]);
							if(obtainHost[j]==1){	obtainFile(obj1,obtainFileName,"etudiant1_rep",0); }
							else if(obtainHost[j]==2){	obtainFile(obj2,obtainFileName,"etudiant2_rep",1); }
							else if(obtainHost[j]==3){	obtainFile(obj3,obtainFileName,"etudiant3_rep",2); }
							else if(obtainHost[j]==4){	obtainFile(obj4,obtainFileName,"etudiant4_rep",3); }
							else if(obtainHost[j]==5){	obtainFile(obj5,obtainFileName,"etudiant5_rep",4); }
							else if(obtainHost[j]==6){	obtainFile(obj6,obtainFileName,"etudiant6_rep",5); }
							else if(obtainHost[j]==7){	obtainFile(obj7,obtainFileName,"etudiant7_rep",6); }
							else if(obtainHost[j]==8){	obtainFile(obj8,obtainFileName,"etudiant8_rep",7); }
						}
					}
					//break;
				}
				else{
					System.out.println("le nom du fichier est invalide");
					//break;
				}
				registerEndTime3 = System.nanoTime();
		        final long registerDuration3 = registerEndTime3
						- registerStartTime3;
				System.out.println("le delai du reponse pour un seul fichier est: "
						+ registerDuration3 + "ns");
				break;
			case 4:
				System.exit(0);
			default:
				System.out.println("\nVeuillez entre un numero entre 1 et 4 :\n");
				break;
			}
		
		}
		}
		finally {
			System.out.println("Hote ferme");
		}
	}
	public static int hashFunction(String s){
		int leng = s.length();
		double h = 0;
		for (int i = 1; i <=leng ; i++) {
			h = h + Math.pow(31, (leng-i)) * s.charAt(i-1);
		}
//		System.out.println(h);
		index = (int) (h%8);
//		System.out.println(index);
		return (int) index;
	}
	public static int selectServer(){
		getKeyValue();
		return hashFunction(key);
		//return Integer.parseInt(key);
	}
	public static void getKeyValue(){
		System.out.println("Enter the key between (100000 - 899999) : ");
		Scanner sc = new Scanner(System.in);
		key = sc.nextLine();
		System.out.println("Enter the respective value of the key-: ");
		Scanner sc1 = new Scanner(System.in);
		value = sc1.nextLine();		
	}
	public static void getPeerFiles(File dir) throws Exception{
		//fileNames = new ArrayList<String>();
		File[] files = dir.listFiles();
		System.out.println("# de fichiers existants: " + files.length);
		
			for (File file : files) {
				fileNames.add(file.getName());
				//System.out.println("file : "+file.getName());
			}
	}
	public void getConfigValues() throws IOException {
		InputStream inputStream = null;
		try {
			Properties prop = new Properties();
			String propFileName = "config.properties";
			inputStream = new FileInputStream(propFileName);
 
			if (inputStream == null) {
				System.out.println("Sorry, unable to find " + propFileName);
    		    return;
			}
 
			prop.load(inputStream);
			Date time = new Date(System.currentTimeMillis());
 
			// get the property value and print it out
			Enumeration<?> e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = prop.getProperty(key);
				//System.out.println("Key : " + key + ", Value : " + value);
				config.put(Integer.parseInt(key), value);
			}
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
	}
	public static String randomValue()
	{
		char[] charList;
	    StringBuilder sb = new StringBuilder();
	    for (char ch = '0'; ch <= '9'; ++ch)
	        sb.append(ch);
	    for (char ch = 'a'; ch <= 'z'; ++ch)
	        sb.append(ch);
	    charList = sb.toString().toCharArray();
		StringBuilder sb1=new StringBuilder();
		Random random=new Random();
	    
		  for(int k=0;k<10000;k++) {
			char ch = charList[random.nextInt(charList.length)];
			sb1.append(ch);
		  }
	   
		String nextLine = sb1.toString();
		return nextLine;
	}
	public static int[] returnKeys()
	{
		int[] obtainHost = new int[8];
		int k=0;
		for(String s1:peer_list1){
			System.out.println(s1);	
			int keys = 0;
		      
	        for(Map.Entry entry: config1.entrySet()){
	            if(s1.equals(entry.getValue())){
	                keys=(Integer) entry.getKey(); //no break, looping entire hashtable
	                break;
	            }
	        }
	        obtainHost[k]=keys;
	        k++;
		}
		return obtainHost;
	}
	public static void registerFile(ObjectOutputStream oos, String repHost, String fileName, int socketId) throws IOException, ClassNotFoundException
	{
		oos.writeObject("put");
		oos.writeObject(fileName);
		oos.writeObject(host_ip+":"+server_port);
		oos.writeObject(repHost);
		ObjectInputStream oin = new ObjectInputStream(socketArray[socketId].getInputStream());
	    Object flag = oin.readObject();
		String inp = (String) flag;
		//System.out.println(flag);
	}
	public static void replecateFile(ObjectOutputStream obj, String fileName, String repDir) throws IOException
	{
		//System.out.println("Replication for file "+fileName +" started ");
		obj.writeObject("replication");
		obj.writeObject(fileName);
		obj.writeObject(repDir);
		//replecateFile(obj3,file_name);
		File myFile = new File(dirname+"/"+fileName);
        FileInputStream fis3 = new FileInputStream(myFile);
        //Socket sock = null;
        try {
        	byte[] mybytearray = new byte[1024];
            //fis3 = new FileInputStream(myFile);
            int count;
            int count1;
            do{
            	count = fis3.read(mybytearray, 0, 1024);
            	if(count==-1)
            	{
            		break;
            	}
            	count1 = count;
                obj.write(mybytearray, 0, count1);
                if(obj != null)obj.flush();
            }while (count  >= 1024);
	        
        }finally {
            fis3.close();
            //System.out.println("Replication done");
        }
	}
	public static void searchFile(ObjectOutputStream osf, String fileName, int socketId) throws IOException, ClassNotFoundException
	{
		osf.writeObject("get");
		osf.writeObject(fileName);
		ObjectInputStream oin21 = new ObjectInputStream(socketArray[socketId].getInputStream());
		Object objValue = oin21.readObject();
		
		try {
			peer_list1 =  (List<String>) objValue;
			if(peer_list1.isEmpty())
			{
				System.out.println("Note : No Peer have "+fileName+" in their shared directory..........");
			}
			else 
			{
				System.out.println("Below Peer have "+fileName+" in their shared directory : ");
				for(String s1:peer_list1){
					System.out.println(s1);	
				}
			}
		} catch(NullPointerException e) {
			System.out.println("Error : No Peer have "+fileName+" in their shared directory..........");
		}
	}
	public static void obtainFile(ObjectOutputStream obj, String fileName, String repDir, int socketId) throws IOException, ClassNotFoundException
	{
		System.out.println("Telechargement de "+fileName +" est commence ");
		obj.writeObject("obtain");
		obj.writeObject(fileName);
		obj.writeObject(repDir);
		FileOutputStream fos = null;
        ObjectInputStream oind = new ObjectInputStream(socketArray[socketId].getInputStream());
        byte[] mybytearray = new byte[1024];
        try {
            fos = new FileOutputStream(dirname+"/"+fileName);
            int count;
            int count1;
            do {
            	Object ob1 = oind.readObject();
            	int buffSize = (int) ob1;
            	if(buffSize!=-1)
            	{
            	count = oind.read(mybytearray,0,1024);
            	
            	count1 = count;
                fos.write(mybytearray, 0, count1);
            	}
            	else {
            		break;
            	}
            } while (count >= 1024);
        } finally {
        	fos.close();
            System.out.println("Fichier telecharge");
        } 
    }
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Tentative de connexion......");
		InetAddress ip;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true){
			try {
				
				serverConnectionSocket = serverSocket.accept();
				System.out.println("l'etablissement du connexion a partir de "+serverConnectionSocket.getInetAddress()+" et le port"+serverConnectionSocket.getLocalPort());
				new p2pClientAsServer(serverConnectionSocket);
	//			obj.fun();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
