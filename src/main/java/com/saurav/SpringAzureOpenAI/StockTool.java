package com.saurav.SpringAzureOpenAI;

import org.springframework.ai.tool.annotation.Tool;

public class StockTool {

    @Tool(description = "Gets the stock price of the brand")
    public String getStockPrice(String brand){
        String name = "getStockPrice";
        System.out.println(name+ " is called by Model");
        if(brand.equalsIgnoreCase("Apple"))
         return  "1012.00";
        else if(brand.equalsIgnoreCase("Google"))
            return  "2014.00";
        else if(brand.equalsIgnoreCase("Facebook"))
            return  "3025.25";
        else if(brand.equalsIgnoreCase("Microsoft"))
            return  "5202.00";
        else
            return "I cant Determine the stock price of : "+brand;
    }
}
