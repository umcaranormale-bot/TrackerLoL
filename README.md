# TrackerLoL
Isso é um código criado com Java e HTML para ler cooldowns, todas as runas das duas árvores(primária e secundária) / This is code created to read cooldowns, all runes from both trees (primary and secondary)..

Nesse código tem alguns detalhes para explicar:

*Antes disso tudo tenha o Java JDK 17, 21+ instalado no computador para o código funcionar!*

1 - Os dados do tempo de recarga dos campeões vem desse site onde tem todos os dados estáticos aqui: https://ddragon.leagueoflegends.com/cdn/14.8.1/data/pt_BR/champion.json. O Data Dragon (DDragon) fornece dados estáticos do League of Legends, incluindo IDs de campeões (campo chave, numérico) e chaves (campo id, baseado em nome), acessíveis por meio de arquivos JSON.

2 - Porém os dados desse site, alguns tempo de recarga de campeões estão desatualizados, pois eles demoram mais pra atualizar esses dados do site do que o próprio jogo.

3 - Então, e quando o Tracker rastrear um campeão, como vou saber que o tempo de recarga está errado? Você pode simplesmente ir no site u.gg ou op.gg e procurar pelo campeão e ver o tempo de recarga das habilidades, eles têm uma equipe gigante e um banco de dados muito mais estruturado para isso, pois eles atualizam manualmente a cada atualização e não dependem totalmente desse site Data DDragon.

4 - Então para que serve o Tracker se ele pode me dar informações erradas sobre o tempo de recarga de algum campeão? Bom, no código disponibilizado que você mesmo pode editar nesta parte do código aqui: <img width="1332" height="882" alt="image" src="https://github.com/user-attachments/assets/90fa2f70-0438-4f26-b3d1-a87deb2b5e25" />

*Como isso funciona? Esse comando que está destacado, nós podemos mudar permanentemente os dados dos campeões que estão desatualizados para os novos dados do tempo de recarga dos campeões vistos no site "u.gg" ou "op.gg".*

*Como podem ver no comando que está destacado, já tem alguns comentários explicando como é feito, basta colocar adicionar o comando: "put("NomeDoCampeao", new String[]{"Q", "W", "E", "R"});"* 

*E substitua os valores como mostrado no exemplo do comando e por fim, dar um rebuild no código para ele carregar essa nova alteração. É quase tipo o que a equipe dos sites u.gg e op.gg faz.*



*5 - SOBRE AS RUNAS PEQUENAS:* Você vai necessitar de uma chave API do site Riot Developer: https://developer.riotgames.com e fazer esses seguintes passos caso você queira que apareça as runas pequenas:

Gere sua chave API que expira em 24 horas a cada nova chave API gerada e copie ela e cole dentro do "chave_riot.txt", sempre que expirar a chave, terá que colar a nova chave API gerada dentro desse "chave_riot.txt", isso tudo para mostrar as runas pequenas. Já a u.gg e o op.gg, eles tem uma chave API permanente sobre isso que a Riot forneceu para eles, bom, isso já é outra história somos apenas usuários comuns utilizando um código comum com um .bat.

*Agora rode o "iniciar.bat" na tela de carregamento após a seleção de campeões! Após alguns minutos(3 minutos provavelmente) as runas pequenas vão aparecer*

Tudo isso para fazer, é caso você queira ter um .bat seu, que você dê apenas um clique e apareça na aba do seu navegador apenas os campeões com os dados que você quer.


------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

This is code created with Java and HTML to read cooldowns, all runes from both trees (primary and secondary).

There are some details to explain in this code:

*Before anything else, you need to have Java JDK 17, 21+ installed on your computer for the code to work!*

1 - The champion cooldown time data comes from this website which has all the static data here: https://ddragon.leagueoflegends.com/cdn/14.8.1/data/pt_BR/champion.json. Data Dragon (DDragon) provides static League of Legends data, including champion IDs (key field, numeric) and keys (id field, name-based), accessible through JSON files.

2 - However, some champion cooldown times on this website are outdated, as they take longer to update this data than the game itself. 3 - So, when the Tracker tracks a champion, how will I know if the cooldown is wrong? You can simply go to the u.gg or op.gg website and search for the champion and see the cooldown times of their abilities. They have a huge team and a much more structured database for this, as they update it manually with each update and don't depend entirely on that Data DDragon website.

4 - So what's the point of the Tracker if it can give me incorrect information about a champion's cooldown? Well, in the provided code that you can edit yourself in this part of the code here: <img width="1332" height="882" alt="image" src="https://github.com/user-attachments/assets/90fa2f70-0438-4f26-b3d1-a87deb2b5e25" />

*How does this work? This highlighted command allows us to permanently change outdated champion data to the new champion cooldown data found on the "u.gg" or "op.gg" websites.

As you can see in the highlighted command, there are already some comments explaining how to do it. Simply add the command: "put("ChampionName", new String[]{"Q", "W", "E", "R"});"

Then replace the values ​​as shown in the command example and finally, rebuild the code to load this new change. It's almost like what the u.gg and op.gg teams do.*

*5 - ABOUT SMALL RUNES:* You will need an API key from the Riot Developer website: https://developer.riotgames.com and follow these steps if you want the small runes to appear:

Generate your API key, which expires in 24 hours with each new API key generated, and copy and paste it into "chave_riot.txt". Whenever the key expires, you will have to paste the new API key generated into "chave_riot.txt". This is all to show the small runes. As for u.gg and op.gg, they have a permanent API key for this that Riot provided them; well, that's another story, we are just ordinary users using a common code with a .bat file.

*Now run "iniciar.bat" on the loading screen after champion selection! After a few minutes (probably 3 minutes) the small runes will appear*

All this is to do, in case you want to have your own .bat file, so that you just click and only the champions with the data you want appear in your browser tab.

