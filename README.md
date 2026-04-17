# TrackerLoL
Isso é um código criado com Java e HTML para ler cooldowns, todas as runas das duas árvores(primária e secundária) / This is code created to read cooldowns, all runes from both trees (primary and secondary)..

Nesse código tem alguns detalhes para explicar:

*Antes disso tudo tenha o Java JDK 17, 21+ instalado no computador para o código funcionar!*

1 - Os dados do tempo de recarga dos campeões vem desse site onde tem todos os dados estáticos aqui: https://ddragon.leagueoflegends.com/cdn/14.8.1/data/pt_BR/champion.json. O Data Dragon (DDragon) fornece dados estáticos do League of Legends, incluindo IDs de campeões (campo chave, numérico) e chaves (campo id, baseado em nome), acessíveis por meio de arquivos JSON.

2 - Porém os dados desse site, alguns tempo de recarga de campeões estão desatualizados, pois eles demoram mais pra atualizar esses dados do site do que o próprio jogo.

3 - Então, e quando o Tracker rastrear um campeão, como vou saber que o tempo de recarga está errado? Você pode simplesmente ir no site u.gg ou op.gg e procurar pelo campeão e ver o tempo de recarga das habilidades, eles têm uma equipe gigante e um banco de dados muito mai estruturado para isso, pois eles atualizam manualmente a cada atualização e não dependem totalmente desse site Data DDragon.

4 - Então para que serve o Tracker se ele pode me dar informações erradas sobre o tempo de recarga de algum campeão? Bom, no código disponibilizado que você mesmo pode editar nesta parte do código aqui: <img width="1332" height="882" alt="image" src="https://github.com/user-attachments/assets/90fa2f70-0438-4f26-b3d1-a87deb2b5e25" />

Como isso funciona? Esse comando que está destacado, nós podemos mudar permanentemente os dados dos campeões que estão desatualizados para os novos dados do tempo de recarga dos campeões vistos no site "u.gg" ou "op.gg", como podem ver no comando que está destacado, já tem alguns comentários explicando como é feito, basta colocar adicionar o comando: "put("NomeDoCampeao", new String[]{"Q", "W", "E", "R"});" e substituir os valores como mostrado no exemplo do comando e por fim, dar um rebuild no código para ele carregar essa nova alteração. É quase tipo o que a equipe dos sites u.gg e op.gg faz.

5 - Sobre as runas pequenas: você necessita de uma chave API do site Riot Developer: https://developer.riotgames.com

Gere sua chave API que expira em 24 horas a cada nova chave API gerada e copie ela e cole dentro do "chave_riot.txt", sempre que expirar a chave, terá que colar a nova chave API gerada dentro desse "chave_riot.txt", isso tudo para mostrar as runas pequenas. Já a u.gg e o op.gg, eles tem uma chave API permanente sobre isso que a Riot forneceu para eles, bom isso já é outra história somos apenas usuários comuns utilizando um código comum com um .bat.

Tudo isso para fazer, é caso você queira ter um .bat seu, que você dê apenas um clique e apareça na aba do seu navegador apenas os campeões com os dados que você quer.

