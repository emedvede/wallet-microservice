--Creates two test wallets
--insert into wallet (currency_id, user_id) values ('EUR','user1');
--insert into wallet (currency_id, user_id) values ('EUR','user2');

insert into wallet (currency_id, user_id) values (1,'user1');
insert into wallet (currency_id, user_id) values (1,'user2');

--Create two test transactions for the first wallet
--insert into transaction (global_id,type_id,amount,wallet_id,currency_id,description)
--values ('test123','C',10,1,'EUR','add funds');
--insert into transaction (global_id,type_id,amount,wallet_id,currency_id,description)
--values ('test2345','D',-10,1,'EUR','remove funds');

insert into transaction (global_id,type_id,amount,wallet_id,currency_id,description)
values ('test123','C',10,1,1,'add funds');
insert into transaction (global_id,type_id,amount,wallet_id,currency_id,description)
values ('test2345','D',-10,1,1,'remove funds');