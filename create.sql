CREATE DATABASE IF NOT EXISTS ad;
USE ad;

CREATE TABLE Item (
  PRIMARY KEY (ItemID),
  ItemID         BIGINT        NOT NULL,
  Name           VARCHAR(255)  NOT NULL,
  Currently      DECIMAL(8,2)  NOT NULL,
  First_Bid      DECIMAL(8,2)  NOT NULL,
  Number_of_Bids INTEGER       NOT NULL,
  Location       VARCHAR(255)  NOT NULL,
  Started        TIMESTAMP     NOT NULL,
  Ends           TIMESTAMP     NOT NULL,
  SellerID       VARCHAR(127)  NOT NULL,
  Description    VARCHAR(4000) NOT NULL,
  Country        VARCHAR(255)  NOT NULL
);

CREATE TABLE ItemCategory (
  PRIMARY KEY (ItemID, Category),
  ItemID   BIGINT       NOT NULL,
           FOREIGN KEY FK_ItemCategory_Item_ItemID(ItemID)
           REFERENCES Item(ItemID)
           ON DELETE CASCADE,
  Category VARCHAR(127) NOT NULL
);

CREATE TABLE ItemBuyPrice (
  PRIMARY KEY (ItemID),
  ItemID    BIGINT       NOT NULL,
            FOREIGN KEY FK_ItemBuyPrice_Item_ItemID(ItemID)
            REFERENCES Item(ItemID)
            ON DELETE CASCADE,
  Buy_Price DECIMAL(8,2) NOT NULL
);

CREATE TABLE ItemCoords (
  PRIMARY KEY (ItemID),
  ItemID    BIGINT NOT NULL,
            FOREIGN KEY FK_ItemCoords_Item_ItemID(ItemID)
            REFERENCES Item(ItemID)
            ON DELETE CASCADE,
  Latitude  DOUBLE NOT NULL,
  Longitude DOUBLE NOT NULL
);

CREATE TABLE User (
  PRIMARY KEY (UserID),
  UserID VARCHAR(127) NOT NULL
);

CREATE TABLE Bidder (
  PRIMARY KEY (BidderId),
  BidderID VARCHAR(127) NOT NULL,
           FOREIGN KEY FK_Bidder_User_BidderID(BidderID)
           REFERENCES User(UserID)
           ON DELETE CASCADE,
  Rating   INTEGER      NOT NULL
);

CREATE TABLE BidderLocation (
  PRIMARY KEY (BidderId),
  BidderID VARCHAR(127) NOT NULL,
           FOREIGN KEY FK_BidderLocation_Bidder_BidderID(BidderID)
           REFERENCES Bidder(BidderID)
           ON DELETE CASCADE,
  Location VARCHAR(255) NOT NULL
);

CREATE TABLE BidderCountry (
  PRIMARY KEY (BidderId),
  BidderID VARCHAR(127) NOT NULL,
           FOREIGN KEY FK_BidderCountry_Bidder_BidderID(BidderID)
           REFERENCES Bidder(BidderID)
           ON DELETE CASCADE,
  Country  VARCHAR(255) NOT NULL
);

CREATE TABLE Bid (
  PRIMARY KEY (BidderId, ItemID, Time),
  BidderID VARCHAR(127) NOT NULL,
           FOREIGN KEY FK_Bid_Bidder_BidderID(BidderID)
           REFERENCES Bidder(BidderID)
           ON DELETE CASCADE,
  ItemID   BIGINT       NOT NULL,
           FOREIGN KEY FK_Bid_Item_ItemID(ItemID)
           REFERENCES Item(ItemID)
           ON DELETE CASCADE,
  Time     TIMESTAMP    NOT NULL,
  Amount   DECIMAL(8,2) NOT NULL
);

CREATE TABLE Seller (
  PRIMARY KEY (SellerID),
          FOREIGN KEY FK_Seller_User_SellerID(SellerID)
          REFERENCES User(UserID)
          ON DELETE CASCADE,
  SellerID VARCHAR(127) NOT NULL,
  Rating   INTEGER      NOT NULL
);