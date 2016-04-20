package com.teamunemployment.breadcrumbs.Network;
/*
 * Handles connection requests to the server. May do quite a lot later on in the piece.
 */
public class LoadBalancer {
    /*"http://104.199.132.109:8080/BreadCrumbs"*/ /*"http://104.155.212.171:8080";*/
    private static String testingIpAddress = "http://192.168.1.89:8080/BreadCrumbs";
    private static String currentLocalIpAddress =/*"http://192.168.1.93:8080/BreadCrumbs";*/ "http://104.199.132.109:8080/BreadCrumbs-1";
    private static String currentDataAddress = /*"http://192.168.1.96:8080/BreadCrumbs";*/ "http://104.199.132.109:8080"; /*"http://ec2-52-27-252-87.us-west-2.compute.amazonaws.com:8080";*///"http://192.168.1.79:8080/BreadCrumbs"; //"http://ec2-52-25-164-125.us-west-2.compute.amazonaws.com:8080";
	public static String RequestServerAddress() {
		return currentLocalIpAddress;
	}
    public static String RequestCurrentDataAddress() {
        return currentDataAddress;
    }
}
