/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.dataloader.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class LoadRateCalculatorTest {
   @Test
   public void testRateCalculatorZeroCompletion() {
      LoadRateCalculator rateCalculator = new LoadRateCalculator();
       rateCalculator.setNumRecords(3600);
       rateCalculator.start();
       String message = rateCalculator.calculateSubTask(0, 0);
       assertEquals("incorrect rate calculation: ", 
               "Processed 0 of 3,600 total records. There are 0 successes and 0 errors.",
               message);

   }
   @Test
   public void testRateCalculatorFirstBatchCompletion() {
      LoadRateCalculator rateCalculator = new LoadRateCalculator();
       rateCalculator.setNumRecords(3600);
       rateCalculator.start();
       String message = rateCalculator.calculateSubTask(0, 0);
       try {
           Thread.sleep(1000);
       } catch (InterruptedException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       message = rateCalculator.calculateSubTask(1, 0);
       assertEquals("incorrect rate calculation: ", 
               "Processed 1 of 3,600 total records. "
               + "There are 1 successes and 0 errors. \nRate: 3,600 records per hour. "
               + "Estimated time to complete: 59 minutes and 59 seconds. ",
               message);
   }

   @Test
   public void testRateCalculatorSecondBatchCompletion() {
      LoadRateCalculator rateCalculator = new LoadRateCalculator();
       rateCalculator.setNumRecords(3600);
       rateCalculator.start();
       String message = rateCalculator.calculateSubTask(0, 0);
       try {
           Thread.sleep(1000);
       } catch (InterruptedException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       message = rateCalculator.calculateSubTask(1, 0);
       try {
           Thread.sleep(1000);
       } catch (InterruptedException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       message = rateCalculator.calculateSubTask(2, 1);
       assertEquals("incorrect rate calculation: ", 
               "Processed 2 of 3,600 total records. "
               + "There are 1 successes and 1 errors. \nRate: 3,600 records per hour. "
               + "Estimated time to complete: 59 minutes and 58 seconds. ",
               message);
   }

   @Test
   public void testRateCalculatorLongTimeForCompletion() {
      LoadRateCalculator rateCalculator = new LoadRateCalculator();
       rateCalculator.setNumRecords(999999999);
       rateCalculator.start();
       String message = rateCalculator.calculateSubTask(0, 0);
       try {
           Thread.sleep(1000);
       } catch (InterruptedException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       message = rateCalculator.calculateSubTask(1, 0);
       assertEquals("incorrect rate calculation: ", 
               "Processed 1 of 999,999,999 total records. "
               + "There are 1 successes and 0 errors.",
               message);
   }
}
