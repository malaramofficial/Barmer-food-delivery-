-- Barmer Food Delivery — demo menu data for the four approved seed restaurants.
-- Safe to run after supabase/seed.sql. Existing matching category/item rows are skipped.

DO $$
DECLARE r uuid; c uuid;
BEGIN
  SELECT id INTO r FROM public.restaurants WHERE name='Marwar Rasoi' LIMIT 1;
  IF r IS NOT NULL THEN
    SELECT id INTO c FROM public.menu_categories WHERE restaurant_id=r AND name='Popular' LIMIT 1;
    IF c IS NULL THEN INSERT INTO public.menu_categories(restaurant_id,name,sort_order) VALUES(r,'Popular',1) RETURNING id INTO c; END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Dal Baati Churma') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Dal Baati Churma','Traditional Rajasthani favourite',180); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Special Thali') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Special Thali','Complete vegetarian thali',220); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Paneer Masala') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Paneer Masala','Fresh paneer curry',160); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Butter Roti') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Butter Roti','Tandoor baked roti',20); END IF;
  END IF;

  SELECT id INTO r FROM public.restaurants WHERE name='Barmer Food Corner' LIMIT 1;
  IF r IS NOT NULL THEN
    SELECT id INTO c FROM public.menu_categories WHERE restaurant_id=r AND name='Popular' LIMIT 1;
    IF c IS NULL THEN INSERT INTO public.menu_categories(restaurant_id,name,sort_order) VALUES(r,'Popular',1) RETURNING id INTO c; END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Veg Noodles') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Veg Noodles','Wok tossed noodles',120); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Manchurian') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Manchurian','Crispy veg Manchurian',130); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Fried Rice') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Fried Rice','Vegetable fried rice',120); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Spring Roll') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Spring Roll','Crispy vegetable rolls',90); END IF;
  END IF;

  SELECT id INTO r FROM public.restaurants WHERE name='Desert Cafe' LIMIT 1;
  IF r IS NOT NULL THEN
    SELECT id INTO c FROM public.menu_categories WHERE restaurant_id=r AND name='Popular' LIMIT 1;
    IF c IS NULL THEN INSERT INTO public.menu_categories(restaurant_id,name,sort_order) VALUES(r,'Popular',1) RETURNING id INTO c; END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Masala Chai') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Masala Chai','Fresh masala tea',30); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Cold Coffee') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Cold Coffee','Chilled cafe-style coffee',90); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Veg Sandwich') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Veg Sandwich','Fresh vegetable sandwich',110); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='French Fries') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'French Fries','Crispy fries',100); END IF;
  END IF;

  SELECT id INTO r FROM public.restaurants WHERE name='Rajputana Kitchen' LIMIT 1;
  IF r IS NOT NULL THEN
    SELECT id INTO c FROM public.menu_categories WHERE restaurant_id=r AND name='Popular' LIMIT 1;
    IF c IS NULL THEN INSERT INTO public.menu_categories(restaurant_id,name,sort_order) VALUES(r,'Popular',1) RETURNING id INTO c; END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Gatte Ki Sabzi') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Gatte Ki Sabzi','Rajasthani gram-flour dumplings',150); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Ker Sangri') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Ker Sangri','Traditional desert vegetable dish',170); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Bajra Roti') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Bajra Roti','Fresh pearl millet roti',35); END IF;
    IF NOT EXISTS (SELECT 1 FROM public.menu_items WHERE restaurant_id=r AND name='Kheer') THEN INSERT INTO public.menu_items(restaurant_id,category_id,name,description,price) VALUES(r,c,'Kheer','Traditional rice pudding',80); END IF;
  END IF;
END $$;
