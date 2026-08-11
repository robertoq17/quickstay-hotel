-- Session VI: Payment is now owned by the independent Payment Service.
-- The monolith must no longer persist payment data in its database.
DROP TABLE IF EXISTS quickstay_write.payments;
