/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.breadcrumbs.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;

/**
 *
 * @author Josiah
 * 
 * Class to hold the details of a polyline, such as start, stop, base event, 
 * head event, distance and transport method etc
 */
public class Polyline {
    // Finals are public now cos fuck you. No seriously I will only be fetching them. These should really be private but I may change that at a later date because I will probably also want to edit them. This is just done for speed
    public final String EncodedPolyline;
    public final int IsEncoded;
    public final int BaseNodeId;
    public final int HeadNodeId;
    public final int TransportMethod;
    
    public Polyline(String encodedPolyline, int baseNodeId, int headNodeId, int transportMethod, int isEncoded) {
        EncodedPolyline = encodedPolyline;
        BaseNodeId = baseNodeId;
        HeadNodeId = headNodeId;
        TransportMethod = transportMethod;
        IsEncoded = isEncoded;
    }
    
    /*
        This method is used to build up encoded polylines.
        https://developers.google.com/maps/documentation/utilities/polylinealgorithm for steps
    
        Currently doesnt work.
    */
    public static String ConvertSignedValueToAscii(Double signedValue) {
    	
        // Convert signed integer
        boolean negative = signedValue < 0;
    	// Multiply by 10^5
        int testRound = (int) Math.round(signedValue*1e5);      
        // Left shift the bits to the left.
        int leftShift = testRound << 1;   
        // Convert to binary.
        String binaryString = Integer.toBinaryString(leftShift);
        // If our initial value was negative, we need to reverse this.
        if (negative) {
        	System.out.println("Number: " + signedValue + " is negative");
            binaryString = invertBinaryString(binaryString);
        }
        // Split our string into an array of bits
        List<String> fives = splitEqually(binaryString, 5);
        System.out.println(fives);
        Iterator<String> fivesItems = fives.iterator();
        
        // This is all super messy but it does some shit
        String result = "";
        while(fivesItems.hasNext()) {
            String item = fivesItems.next();
            if (fivesItems.hasNext()) {
            	item = "1"+item;
                System.out.println(item);
                if (item.length() == 6) {
                	int baseValue = Integer.parseInt(item, 2);
                	System.out.println("base int converted from binary bit");
                	int added63Value = baseValue + 63;
                	char ch = (char) added63Value;
                	System.out.println("ASCII value of:" + added63Value + " => "+ch);
                	result+=ch;
                }
            } else {
            	item = "0"+item;
                System.out.println(item);
                if (item.length() == 6) {
                	int baseValue = Integer.parseInt(item, 2);
                	System.out.println("base int converted from binary bit");
                	int added63Value = baseValue + 63;
                	char ch = (char) added63Value;
                	System.out.println("ASCII value of:" + added63Value + " => "+ch);
                	result += ch;
                }
            }    
        }
        
        return result;
    }
    
    private static List<String> splitEqually(String text, int size) {
           // Give the list the right capacity to start with. You could use an array
           // instead if you wanted.
           List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

           for (int start = text.length(); start > 0; start -= size) {
               ret.add(text.substring(start, Math.min(text.length(), start + size)));
           }
           return ret;
       }
        
    // Simple method to invert a binary String.
    private static String invertBinaryString(String string) {

        String invertedString = "";
        for (int i = 0; i < string.length(); i++) {
            char tempChar = string.charAt(i);
            // add the opposite to our inverted string.
            if (tempChar == '0') {
                invertedString = invertedString.concat("1");
            } else {
                invertedString = invertedString.concat("0");
            }
        }

        return invertedString;
    }
}
