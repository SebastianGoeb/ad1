USE ad;

# 1. Number of users
SELECT COUNT(*)
FROM User;

# 2. Number of items in "New York"
SELECT COUNT(*)
FROM Item
WHERE Location = BINARY 'New York';

# 3. Number of auction with 4 deduped categories
SELECT COUNT(*)
FROM Item
WHERE 4 = (
  SELECT COUNT(*)
  FROM ItemCategory
  WHERE ItemCategory.ItemID = Item.ItemID
);

# 4. IDs of current unsold auctions and their highest bid (excluding those without bids)
SELECT Item.ItemID, highestBid.Amount
FROM Item
INNER JOIN (
  SELECT ItemID, MAX(Amount) Amount
  FROM Bid
  GROUP BY ItemID
) highestBid ON Item.ItemID = highestBid.ItemID
WHERE Item.Ends > '2001-12-20 00:00:01'
AND highestBid.Amount > Item.First_Bid;

# 5. Sellers with rating over 1000
SELECT COUNT(*)
FROM Seller
WHERE Rating > 1000;

# 6. Number of users who are both sellers and bidders
SELECT COUNT(*)
FROM User
INNER JOIN Seller ON User.UserID = Seller.SellerID
INNER JOIN Bidder ON User.UserID = Bidder.BidderID

# 7. Number of categories that include at least one item with a bid of over $100
SELECT *
FROM ItemCategory
WHERE EXISTS (
  SELECT ItemID
  FROM Item
  WHERE ItemCategory.ItemID = Item.ItemID
  AND EXISTS (
    SELECT *
    FROM Bid
    WHERE Item.ItemID = Bid.ItemID
    AND Bid.Amount > 100
    AND Bid.Amount > Item.First_Bid
  )
)