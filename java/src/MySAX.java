/* Parser skeleton for processing item-???.xml files. Must be compiled in
 * JDK 1.5 or above.
 *
 * Instructions:
 *
 * This program processes all files passed on the command line (to parse
 * an entire diectory, type "java MyParser myFiles/*.xml" at the shell).
 *
 */

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Stack;

public class MySAX extends DefaultHandler {

    private static final DateFormat XML_DATE_FORMAT = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
    private static final DateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private enum ElementType {
        Items, Item, Name, Category, Currently, Buy_Price, First_Bid, Number_of_Bids, Bids, Bid, Bidder, Time,
        Amount, Location, Country, Started, Ends, Seller, Description
    }

    // Domain objects
    private Item item;
    private ItemCategory itemCategory;
    private ItemBuyPrice itemBuyPrice;
    private Bid bid;
    private Bidder bidder;
    private BidderLocation bidderLocation;
    private BidderCountry bidderCountry;
    private Seller seller;
    private ItemCoords itemCoords;

    // PrintWriters for writing to file
    private PrintWriter itemWriter;
    private PrintWriter itemCategoryWriter;
    private PrintWriter itemBuyPriceWriter;
    private PrintWriter bidWriter;
    private PrintWriter bidderWriter;
    private PrintWriter bidderLocationWriter;
    private PrintWriter bidderCountryWriter;
    private PrintWriter sellerWriter;
    private PrintWriter itemCoordsWriter;

    // Record element types currently being processed
    private Stack<ElementType> elementStack = new Stack<ElementType>();

    // Record primary keys (potentially multiple, concatenated IDs) for deduplication
    private HashSet<String> itemPKs = new HashSet<String>();
    private HashSet<String> itemCategoryPKs = new HashSet<String>();
    private HashSet<String> itemBuyPricePKs = new HashSet<String>();
    private HashSet<String> bidPKs = new HashSet<String>();
    private HashSet<String> bidderPKs = new HashSet<String>();
    private HashSet<String> bidderLocationPKs = new HashSet<String>();
    private HashSet<String> bidderCountryPKs = new HashSet<String>();
    private HashSet<String> sellerPKs = new HashSet<String>();
    private HashSet<String> itemCoordsPKs = new HashSet<String>();

    public static void main(String args[]) throws Exception {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        MySAX handler = new MySAX();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);

        // Parse each file provided on the
        // command line.
        for (String arg : args) {
            xr.parse(new InputSource(new FileReader(arg)));
        }
    }

    private static String join(String delim, Object... objects) {
        StringBuilder sb = new StringBuilder();
        for (Object o : objects) {
            sb.append(delim).append(o);
        }
        if (objects.length > 0) {
            sb.delete(0, delim.length());
        }
        return sb.toString();
    }

    private static void requireNonNull(Object... objects) {
        for (Object o : objects) {
            if (o == null) {
                throw new NullPointerException("Some arguments were null: " + join("\t", objects));
            }
        }
    }

    /* Returns the amount (in XXXXX.xx format) denoted by a money-string
     * like $3,453.23. Returns the input if the input is an empty string.
     */
    static String strip(String money) {
        if (money.equals(""))
            return money;
        else {
            double am = 0.0;
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            try {
                am = nf.parse(money).doubleValue();
            } catch (ParseException e) {
                System.out.println("This method should work for all " +
                        "money values you find in our data.");
                System.exit(20);
            }
            nf.setGroupingUsed(false);
            return nf.format(am).substring(1);
        }
    }

    static String format(String timestamp) {
        String output = timestamp;
        try {
            output = SQL_DATE_FORMAT.format(XML_DATE_FORMAT.parse(timestamp));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return output;
    }

    private MySAX() {
        super();
        try {
            itemWriter = new PrintWriter("item.csv");
            itemCategoryWriter = new PrintWriter("itemCategory.csv");
            itemBuyPriceWriter = new PrintWriter("itemBuyPrice.csv");
            bidWriter = new PrintWriter("bid.csv");
            bidderWriter = new PrintWriter("bidder.csv");
            bidderLocationWriter = new PrintWriter("bidderLocation.csv");
            bidderCountryWriter = new PrintWriter("bidderCountry.csv");
            sellerWriter = new PrintWriter("seller.csv");
            itemCoordsWriter = new PrintWriter("itemCoords.csv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////
    public void startDocument() {
    }

    public void endDocument() {
        if (!elementStack.isEmpty()) {
            throw new RuntimeException("Forgot to write out some elements somewhere");
        }
        itemWriter.flush();
        itemCategoryWriter.flush();
        itemBuyPriceWriter.flush();
        bidWriter.flush();
        bidderWriter.flush();
        bidderLocationWriter.flush();
        bidderCountryWriter.flush();
        sellerWriter.flush();
        itemCoordsWriter.flush();
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
        ElementType newElement = ElementType.valueOf(name);
        ElementType parentElement = elementStack.isEmpty() ? null : elementStack.peek();
        switch (newElement) {
            case Item:
                item = new Item();
                item.ItemID = atts.getValue("ItemID");
                break;
            case Category:
                itemCategory = new ItemCategory();
                itemCategory.ItemID = item.ItemID;
                break;
            case Buy_Price:
                itemBuyPrice = new ItemBuyPrice();
                itemBuyPrice.ItemID = item.ItemID;
                break;
            case Bid:
                bid = new Bid();
                bid.ItemID = item.ItemID;
                break;
            case Bidder:
                bidder = new Bidder();
                bidder.BidderID = atts.getValue("UserID");
                bidder.Rating = atts.getValue("Rating");
                bid.BidderID = bidder.BidderID;
                break;
            case Location:
                String lat = atts.getValue("Latitude");
                String lng = atts.getValue("Longitude");
                if (parentElement == ElementType.Item) {
                    if (lat != null && lng != null) {
                        itemCoords = new ItemCoords();
                        itemCoords.ItemID = item.ItemID;
                        itemCoords.Latitude = lat;
                        itemCoords.Longitude = lng;
                    }
                } else if (parentElement == ElementType.Bidder) {
                    if (lat == null && lng == null) {
                        bidderLocation = new BidderLocation();
                        bidderLocation.BidderID = bidder.BidderID;
                    } else {
                        throw new RuntimeException("Bidder location shouldn't have lat & lng");
                    }
                } else {
                    throw new RuntimeException("Unexpected Location child of " + parentElement);
                }
                break;
            case Country:
                if (parentElement == ElementType.Bidder) {
                    bidderCountry = new BidderCountry();
                    bidderCountry.BidderID = bidder.BidderID;
                } else if (parentElement != ElementType.Item) {
                    throw new RuntimeException("Unexpected Country child of " + parentElement);
                }
                break;
            case Seller:
                seller = new Seller();
                seller.SellerID = atts.getValue("UserID");
                seller.Rating = atts.getValue("Rating");
                item.SellerID = seller.SellerID;
                break;
        }

        elementStack.push(newElement);
    }

    public void endElement(String uri, String name, String qName) {
        ElementType currentElement = elementStack.pop();
        ElementType parentElement = elementStack.isEmpty() ? null : elementStack.peek();
        switch (currentElement) {
            case Item:
                // Write item
                if (!itemPKs.contains(item.getPK())) {
                    itemPKs.add(item.getPK());
                    itemWriter.println(item);
                } else {
                    throw new RuntimeException("Duplicated Item");
                }
                item = null;
                if (itemCoords != null) {
                    // Write locationCoords
                    if (!itemCoordsPKs.contains(itemCoords.getPK())) {
                        itemCoordsPKs.add(itemCoords.getPK());
                        itemCoordsWriter.println(itemCoords);
                    } else {
                        throw new RuntimeException("Duplicated ItemCoords for same Item");
                    }
                    itemCoords = null;
                }
                break;
            case Category:
                // Write itemCategory
                if (!itemCategoryPKs.contains(itemCategory.getPK())) {
                    itemCategoryPKs.add(itemCategory.getPK());
                    itemCategoryWriter.println(itemCategory);
                } else {
                    throw new RuntimeException("Duplicated Category for same Item");
                }
                itemCategory = null;
                break;
            case Buy_Price:
                // Write itemBuyPrice
                if (!itemBuyPricePKs.contains(itemBuyPrice.getPK())) {
                    itemBuyPricePKs.add(itemBuyPrice.getPK());
                    itemBuyPriceWriter.println(itemBuyPrice);
                } else {
                    throw new RuntimeException("Duplicated Buy_Price for same Item");
                }
                itemBuyPrice = null;
                break;
            case Bid:
                // Write bid
                if (!bidPKs.contains(bid.getPK())) {
                    bidPKs.add(bid.getPK());
                    bidWriter.println(bid);
                } else {
                    throw new RuntimeException("Duplicated Bid for same Bidder, Item, and Time");
                }
                bid = null;
                break;
            case Bidder:
                // Write bidder, skip duplicates
                if (!bidderPKs.contains(bidder.getPK())) {
                    bidderPKs.add(bidder.getPK());
                    bidderWriter.println(bidder);
                }
                bidder = null;
                break;
            case Location:
                if (parentElement == ElementType.Bidder) {
                    // Write bidderLocation, skip duplicates
                    if (!bidderLocationPKs.contains(bidderLocation.getPK())) {
                        bidderLocationPKs.add(bidderLocation.getPK());
                        bidderLocationWriter.println(bidderLocation);
                    }
                    bidderLocation = null;
                }
                break;
            case Country:
                if (parentElement == ElementType.Bidder) {
                    // Write bidderCountry, skip duplicates
                    if (!bidderCountryPKs.contains(bidderCountry.getPK())) {
                        bidderCountryPKs.add(bidderCountry.getPK());
                        bidderCountryWriter.println(bidderCountry);
                    }
                    bidderCountry = null;
                }
                break;
            case Seller:
                // Write seller, skip duplicates
                if (!sellerPKs.contains(seller.getPK())) {
                    sellerPKs.add(seller.getPK());
                    sellerWriter.println(seller);
                }
                seller = null;
                break;
        }
    }

    public void characters(char ch[], int start, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + length; i++) {
            sb.append(ch[i]);
        }
        String value = sb.toString();

        ElementType currentElement = elementStack.pop();
        ElementType parentElement = elementStack.isEmpty() ? null : elementStack.peek();
        switch (currentElement) {
            case Name:
                item.Name += value;
                break;
            case Category:
                itemCategory.Category += value;
                break;
            case Currently:
                item.Currently += value;
                break;
            case Buy_Price:
                itemBuyPrice.Buy_Price += value;
                break;
            case First_Bid:
                item.First_Bid += value;
                break;
            case Number_of_Bids:
                item.Number_of_Bids += value;
                break;
            case Time:
                bid.Time += value;
                break;
            case Amount:
                bid.Amount += value;
                break;
            case Location:
                if (parentElement == ElementType.Item) {
                    item.Location += value;
                } else if (parentElement == ElementType.Bidder) {
                    bidderLocation.Location += value;
                } else {
                    throw new RuntimeException("Unexpected Location child of " + parentElement);
                }
                break;
            case Country:
                if (parentElement == ElementType.Item) {
                    item.Country += value;
                } else if (parentElement == ElementType.Bidder) {
                    bidderCountry.Country += value;
                } else {
                    throw new RuntimeException("Unexpected Country child of " + parentElement);
                }
                break;
            case Started:
                item.Started += value;
                break;
            case Ends:
                item.Ends += value;
                break;
            case Description:
                item.Description += value;
                break;
        }
        elementStack.push(currentElement);
    }

    private static class Item {
        private String ItemID;
        private String Name = "";
        private String Currently = "";
        private String First_Bid = "";
        private String Number_of_Bids = "";
        private String Location = "";
        private String Started = "";
        private String Ends = "";
        private String SellerID;
        private String Description = "";
        private String Country;

        private String getPK() {
            return ItemID;
        }

        @Override
        public String toString() {
            requireNonNull(ItemID, Name, Currently, First_Bid, Number_of_Bids, Location, Started, Ends, SellerID,
                    Description, Country);
            return ItemID
                    + '\t' + Name
                    + '\t' + strip(Currently)
                    + '\t' + strip(First_Bid)
                    + '\t' + Number_of_Bids
                    + '\t' + Location
                    + '\t' + format(Started)
                    + '\t' + format(Ends)
                    + '\t' + SellerID
                    + '\t' + Description.substring(0, Math.min(Description.length(), 4000))
                    + '\t' + Country;
        }
    }

    private static class ItemCategory {
        private String ItemID;
        private String Category = "";

        private String getPK() {
            return ItemID + Category;
        }

        @Override
        public String toString() {
            requireNonNull(ItemID, Category);
            return ItemID + '\t' + Category;
        }
    }

    private static class ItemBuyPrice {
        private String ItemID;
        private String Buy_Price = "";

        private String getPK() {
            return ItemID;
        }

        @Override
        public String toString() {
            requireNonNull(ItemID, Buy_Price);
            return ItemID + '\t' + strip(Buy_Price);
        }
    }

    private static class Bid {
        private String BidderID;
        private String ItemID;
        private String Time = "";
        private String Amount = "";

        private String getPK() {
            return BidderID + ItemID + Time;
        }

        @Override
        public String toString() {
            requireNonNull(BidderID, ItemID, Time, Amount);
            return BidderID
                    + '\t' + ItemID
                    + '\t' + format(Time)
                    + '\t' + strip(Amount);
        }
    }

    private static class Bidder {
        private String BidderID;
        private String Rating;

        private String getPK() {
            return BidderID;
        }

        @Override
        public String toString() {
            requireNonNull(BidderID, Rating);
            return BidderID + '\t' + Rating;
        }
    }

    private static class BidderLocation {
        private String BidderID;
        private String Location = "";

        private String getPK() {
            return BidderID;
        }

        @Override
        public String toString() {
            requireNonNull(BidderID, Location);
            return BidderID + '\t' + Location;
        }
    }

    private static class BidderCountry {
        private String BidderID;
        private String Country = "";

        private String getPK() {
            return BidderID;
        }

        @Override
        public String toString() {
            requireNonNull(BidderID, Country);
            return BidderID + '\t' + Country;
        }
    }

    private static class Seller {
        private String SellerID;
        private String Rating;

        private String getPK() {
            return SellerID;
        }

        @Override
        public String toString() {
            requireNonNull(SellerID, Rating);
            return SellerID + '\t' + Rating;
        }
    }

    private static class ItemCoords {
        private String ItemID;
        private String Latitude;
        private String Longitude;

        private String getPK() {
            return ItemID;
        }

        @Override
        public String toString() {
            requireNonNull(ItemID, Latitude, Longitude);
            return ItemID
                    + '\t' + Latitude
                    + '\t' + Longitude;
        }
    }
}