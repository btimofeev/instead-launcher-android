-- $Name: Test Game$
-- $Name(ru): Тестовая игра$
-- $Author: Test Author$
-- $Author(ru): Тест Автор$
-- $Version: 1.0$
-- $Info: Game for test$
-- $Info(ru): Игра для теста$


require "fmt"
fmt.para = true

game.act = 'Гм...';
game.use = 'Не сработает.';
game.inv = 'Зачем это мне?';

room {
	nam = 'main';
	disp = "Главная комната";
	dsc = [[Вы в большой комнате.]];
}