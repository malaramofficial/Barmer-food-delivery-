-- Demo seed data for Barmer. Replace with admin-approved partner data in production.
insert into public.restaurants(name,cuisine,address,area,phone,rating,delivery_fee,is_open,is_approved,latitude,longitude)
values
('Marwar Rasoi','North Indian • Thali','Barmer, Rajasthan','Barmer City',null,4.6,25,true,true,25.7521,71.3960),
('Barmer Food Corner','Chinese • Fast Food','Barmer, Rajasthan','Barmer City',null,4.4,20,true,true,25.7480,71.3920),
('Desert Cafe','Cafe • Snacks','Barmer, Rajasthan','Barmer City',null,4.7,15,true,true,25.7550,71.4000),
('Rajputana Kitchen','Rajasthani • Veg','Barmer, Rajasthan','Barmer City',null,4.5,30,true,true,25.7450,71.4010)
on conflict do nothing;
